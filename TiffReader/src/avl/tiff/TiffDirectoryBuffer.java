package avl.tiff;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.Timer;

public class TiffDirectoryBuffer {
    private final long MillisecondsToKeepInactiveTiles = 30000;
    private final BufferingStatus tileFetchingStatus[][];
    private final long tileTime[][];
    private final BufferedImage tiles[][];
    private final int  imageWidth, imageLength;
    private final short compression;
    private final int tileLength, tileWidth, tilesAcrossW, tilesDownL;
    private double zoomLevel;
    private ArrayList<Point> tilesInView;
    private final int photometric;
    private final Timer timerFreeMemory;

    public TiffDirectoryBuffer(Properties p) {
        imageWidth = Integer.parseInt(p.get("ImageWidth").toString());
        imageLength = Integer.parseInt(p.get("ImageLength").toString());
        compression = Short.valueOf(p.get("Compression").toString());

        tileLength = Integer.parseInt(p.get("TileLength").toString());
        tileWidth = Integer.parseInt(p.get("TileWidth").toString());
        tilesAcrossW = Integer.parseInt(p.get("TilesAcrossW").toString());
        tilesDownL = Integer.parseInt(p.get("TilesDownL").toString());
        if (p.containsKey("Photometric")){
            photometric = Integer.parseInt(p.getProperty("Photometric"));
        } else {
            photometric = 2;
        }
        //imageDescription = p.get("ImageDescription").toString();
        String temp = p.get("ZoomLevel").toString();
        try {
            zoomLevel = Double.valueOf(temp);
        } catch (Exception e){
            zoomLevel = Double.NaN;
        }
        
        tiles = new BufferedImage[tilesAcrossW][tilesDownL];
        tileFetchingStatus = new BufferingStatus[tilesAcrossW][tilesDownL];
        for (int i = 0; i < tilesAcrossW; i++) {
            for (int j = 0; j < tilesDownL; j++) {
                tileFetchingStatus[i][j] = BufferingStatus.NOT_BUFFERED;
            }
        }
        tileTime = new long[tilesAcrossW][tilesDownL];
        timerFreeMemory = new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long mark = System.currentTimeMillis() - MillisecondsToKeepInactiveTiles;
                removeTilesLastUsedBefore(mark);
                Runtime rt = Runtime.getRuntime();
//                if (rt.freeMemory() < (rt.totalMemory()*1/4)){
//                    System.out.println("before: free="+String.valueOf(rt.freeMemory())+" total="+String.valueOf(rt.totalMemory()));
//                    removeTilesLastUsedBefore(Long.MAX_VALUE);
//                    System.out.println("after:  free="+String.valueOf(rt.freeMemory())+" total="+String.valueOf(rt.totalMemory()));
//                }
            }
        });
        timerFreeMemory.setRepeats(true);
    }
    
    public void close(){
        if ((timerFreeMemory != null)&& timerFreeMemory.isRunning()){
            timerFreeMemory.stop();
        }
        clearBuffers();
    }
    
    public long getTileTime(int w, int l){
        return tileTime[w][l];
    }
    
    public ArrayList<Point> getTilesInView() {
        return tilesInView;
    }
    public void setTilesInView(ArrayList<Point> tilesInView) {
        this.tilesInView = tilesInView;
    }
    private byte jpegTables[] = null;
    
    public void setupDecoder(byte jpegTables[]) throws IOException {
        this.jpegTables = jpegTables;
    }
    
    public boolean isInView(int x, int y){
        if (tilesInView == null){
            return false;
        }
        for (Point tile:tilesInView){
            if ((tile.x == x) && (tile.y == y)){
                return true;
            }
        }
        return false;
    }
    
    public void clearBuffers(){
        for (int i = 0; i < tiles.length; i++){
            for (int j = 0; j < tiles[i].length; j++){
                if (isInView(i, j)){
                    continue;
                }
                tiles[i][j] = null;
                tileFetchingStatus[i][j]  = BufferingStatus.NOT_BUFFERED;
                tileTime[i][j] = -1;
            }
        }
    }
    
    public double getZoomLevel() {
        return zoomLevel;
    }

    public BufferingStatus getTileFechingStatus(int x, int y) {
        return tileFetchingStatus[x][y];
    }
    
    public boolean isATileFeching(){
        for (int i = 0; i < tileFetchingStatus.length; i++){
            BufferingStatus[] sub = tileFetchingStatus[i];
            for (int j = 0; j < sub.length; j++){
                if (sub[j] != BufferingStatus.BUFFERED){
                    return true;
                }
            }
        }
        return false;
    }

    public void setTileFechingStatus(int x, int y, BufferingStatus s) {
        tileFetchingStatus[x][y] = s;
    }

    public BufferedImage getTile(int x, int y) {
        tileTime[x][y] = System.currentTimeMillis();
        return tiles[x][y];
    }
    
    public void setTile(int x, int y, BufferedImage img) {
        timerFreeMemory.start();
        if (img == null){
            tileFetchingStatus[x][y] = BufferingStatus.NOT_BUFFERED;
        } else {
            tiles[x][y] = img;
            tileFetchingStatus[x][y] = BufferingStatus.BUFFERED;
            tileTime[x][y] = System.currentTimeMillis();
        }
    }
    public BufferedImage setTile(int x, int y, byte b[]) {
        BufferedImage img = convertByteArrayToImage(b);
        setTile( x, y, img);
        return img;
    }
    
    private void convertYCbCrToRGB(BufferedImage img) {
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int YCbCr = img.getRGB(i, j);
                int y = (YCbCr >> 16) & 0xFF;
                int cb = (YCbCr >> 8) & 0xFF;
                int cr = YCbCr & 0xFF;
                
                double Y = (double) y;
                double Cb = (double) cb;
                double Cr = (double) cr;

                int r = (int) (Y + 1.40200 * (Cr - 0x80));
                int g = (int) (Y - 0.34414 * (Cb - 0x80) - 0.71414 * (Cr - 0x80));
                int b = (int) (Y + 1.77200 * (Cb - 0x80));

                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                int rgb = r;
                rgb = (rgb << 8) + g;
                rgb = (rgb << 8) + b;
                img.setRGB(i, j, rgb);
            }
        }
    }
    
    List<ImageReader> imageReaders = Collections.synchronizedList(new ArrayList<ImageReader>());
    public BufferedImage convertByteArrayToImage(byte b[]) { 
        ImageReader imageReader;
        BufferedImage img = null;
        try {
            imageReader = imageReaders.remove(0);
        } catch (IndexOutOfBoundsException ex){
            imageReader = ImageIO.getImageReadersBySuffix("jpeg").next();
        }
        try {
            int idx = 0;
            byte full[] = new byte[jpegTables.length+b.length];
            System.arraycopy(b, 0, full, idx, 2);
            idx +=2;
            System.arraycopy(jpegTables, 2, full, idx, jpegTables.length-4);
            idx += jpegTables.length-4;
            System.arraycopy(b, 2, full, idx, b.length-2);
            ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(full));
            imageReader.setInput(iis, true, true);
            img = imageReader.read(0);
        } catch (IOException ex) {
            Logger.getLogger(TiffDirectoryBuffer.class.getName()).log(Level.SEVERE, null, ex);
        }
        imageReaders.add(imageReader);
        return img;
    }
    
    public int getImageWidth() {
        return imageWidth;
    }
    public int getImageLength() {
        return imageLength;
    }

    public short getCompression() {
        return compression;
    }

    public int getTileLength() {
        return tileLength;
    }
    public int getTileWidth() {
        return tileWidth;
    }

    public int getTilesAcrossW() {
        return tilesAcrossW;
    }
    public int getTilesDownL() {
        return tilesDownL;
    }

    public void removeTilesLastUsedBefore(long mark){
        // removes tiles not used before mark time
        for (int i = 0; i < tiles.length; i++){
            for (int j = 0; j < tiles[i].length; j++){
                if (tiles[i][j] == null){
                    continue;
                }
                if (isInView(i, j)){
                    continue;
                }
                if (tileTime[i][j]<mark){
                    tiles[i][j] = null;
                    tileFetchingStatus[i][j]  = BufferingStatus.NOT_BUFFERED;
                    tileTime[i][j] = -1;
                }
            }
        }
    }
}
