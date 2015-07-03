package avl.tiff;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JFrame;
import javax.xml.bind.DatatypeConverter;

public class TiffDirectory {

    private RandomAccessFileLittleEndian rafle;
    private long fileOffset;
    private long imageWidth, imageLength;
    private short bitsPerSample;
    private long numOfDirEntries, tileOffsets[] = null;
    boolean isBigTiff;
    private int offsetByteSize;
    private short jpegProc, compression;
    private int tileWidth = -1, tileLength;
    private long jpegQTables[];
    private long jpegDCTables[];
    private long jpegACTables[];
    private byte jpegTables[];
    private int tilesAcrossW, tilesDownL, tileByteCounts[];
    private File file;
    private String imageDescription;
    private String zoomLevel;
    private int photometric;

    public int getPhotometric() {
        return photometric;
    }
    
    public String getZoomLevel() {
        return zoomLevel;
    }
    
    public boolean isBigTiff() {
        return isBigTiff;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileLength() {
        return tileLength;
    }

    public long[] getJpegQTables() {
        return jpegQTables;
    }

    public long[] getJpegDCTables() {
        return jpegDCTables;
    }

    public long[] getJpegACTables() {
        return jpegACTables;
    }

    public byte[] getJpegTables() {
        return jpegTables;
    }

    public long getTilesAcrossW() {
        return tilesAcrossW;
    }

    public long getTilesDownL() {
        return tilesDownL;
    }

    public short getJpegProc() {
        return jpegProc;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public long getImageWidth() {
        return imageWidth;
    }

    public long getImageLength() {
        return imageLength;
    }

    public Short getBitsPerSample() {
        return bitsPerSample;
    }

    public int getOffsetByteSize() {
        return offsetByteSize;
    }

    public long getNumOfDirEntries() {
        return numOfDirEntries;
    }

    TiffDirectory(File file, long fileOffset) throws FileNotFoundException, IOException {
        this.file = file;
        rafle = new RandomAccessFileLittleEndian(file, "r");
        short tiffVersion = TiffFile.getTiffVersion(file);
        isBigTiff = TiffFile.isBigTiff(tiffVersion);
        offsetByteSize = TiffFile.getOffsetByteSize(file);
        this.fileOffset = fileOffset;
        readDirectoryEntries(file);
        attemptToFindZoomLevel();
    }
    
    public void close() throws IOException{
        rafle.close();
    }
    
    private void attemptToFindZoomLevel(){
        if (imageDescription == null) {
            return;
        }
        zoomLevel = "";
        if (imageDescription.contains("Aperio ImageComp")||imageDescription.contains("Aperio Image Library")){
            if (imageDescription.contains("label")){
                zoomLevel = "label";
            } else if (imageDescription.contains("macro")) {
                zoomLevel = "macro";
            } else {
                int end;
                int start;
                String ss;
                if (imageDescription.contains("[") & imageDescription.contains("]")) {
                    end = imageDescription.indexOf("]");
                    start = imageDescription.lastIndexOf(" ", end);
                } else {
                    start = imageDescription.indexOf("\n");
                    end = imageDescription.indexOf(" ", start);
                }
                ss = imageDescription.substring(start + 1, end);
                String[] s = ss.split("x");
                if (s.length == 2) {
                    double w = Double.valueOf(s[0]);
                    double l = Double.valueOf(s[1]);
                    double rw = this.imageWidth / w;
                    double rh = this.imageLength / l;
                    double r = (rw+rh)/2.0;
                    zoomLevel = Double.toString(r);
                }
            }
        } else if (imageDescription.contains("x")) {
            String[] s = imageDescription.split("x");
            if (s.length == 2) {
                double w = Double.valueOf(s[0]);
                double l = Double.valueOf(s[1]);
                double rw = this.imageWidth / w;
                double rh = this.imageLength / l;
                double r = (rw + rh) / 2.0;
                zoomLevel = Double.toString(r);
            }
        }
    }

    ArrayList<Number> getElements(RandomAccessFileLittleEndian s, short type, long count) throws IOException {
        ArrayList<Number> a = new ArrayList<Number>();
        if (1 == type | 2 == type | 6 == type | 7 == type) {
            for (int i = 0; i < count; i++) {
                a.add(s.readNBytes(1));
            }
        } else if (3 == type | 8 == type) {
            for (int i = 0; i < count; i++) {
                a.add(s.readNBytes(2));
            }
        } else if (4 == type | 9 == type) {
            for (int i = 0; i < count; i++) {
                a.add(s.readNBytes(4));
            }
        } else if (11 == type) {
            for (int i = 0; i < count; i++) {
                a.add(s.readNBytes(8));
            }
        } else if (12 == type) {
            for (int i = 0; i < count; i++) {
                a.add(s.readNBytes(8));
            }
        }
        return a;
    }

    private void readDirectoryEntries(File file) throws IOException {
        // getNumOfDirEntries 
        rafle.seek(fileOffset);
        if (isBigTiff) {
            numOfDirEntries = rafle.readNBytes(8).longValue();
        } else {
            numOfDirEntries = rafle.readNBytes(2).longValue();
        }

        for (int i = 0; i < numOfDirEntries; i++) {
            if (isBigTiff) {
                rafle.seek(i * 20 + 8 + fileOffset);
            } else {
                rafle.seek(i * 12 + 2 + fileOffset);
            }
            TiffTag tagID = TiffTag.fromShort(rafle.readShort());
            short dataType = rafle.readShort();
            long elementCount = rafle.readNBytes(offsetByteSize).longValue();
            if (elementCount > 1) { // if the element is really a pointer to the element list
                long temp = rafle.readNBytes(offsetByteSize).longValue();
                rafle.seek(temp);
            }
            ArrayList<Number> elements = getElements(rafle, dataType, elementCount);
            switch (tagID) {
                case PhotometricInterpretation:
                    photometric = elements.get(0).intValue();
                    break;
                case ImageDescription:
                    char temp[] = new char[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        temp[e] = (char) elements.get(e).byteValue();
                    }
                    imageDescription = new String(temp);
                case NewSubfileType:
                    break;
                case ImageWidth:
                    imageWidth = elements.get(0).intValue();
                    break;
                case ImageLength:
                    imageLength = elements.get(0).intValue();
                    break;
                case BitsPerSample:
                    bitsPerSample = elements.get(0).shortValue();
                    break;
                case Compression:
                    compression = elements.get(0).shortValue();
                    break;
                case JPEGProc:
                    jpegProc = elements.get(0).shortValue();
                    break;
                case StripByteCounts: 
                    tileByteCounts = new int[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        tileByteCounts[e] = elements.get(e).intValue();
                    }
                    break;
                case StripOffsets:
                    tileOffsets = new long[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        tileOffsets[e] = elements.get(e).longValue();
                    }
                    break;
                case RowsPerStrip:
                    tileLength = elements.get(0).intValue();
                    break;
                case TileWidth:
                    tileWidth = elements.get(0).intValue();
                    break;
                case TileLength:
                    tileLength = elements.get(0).intValue();
                    break;
                case TileOffsets:
                    tileOffsets = new long[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        tileOffsets[e] = elements.get(e).longValue();
                    }
                    break;
                case TileByteCounts:
                    tileByteCounts = new int[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        tileByteCounts[e] = elements.get(e).intValue();
                    }
                    break;
                case JPEGTables:
                    jpegTables = new byte[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        jpegTables[e] = elements.get(e).byteValue();
                    }
                    break;
                case JPEGQTables:
                    jpegQTables = new long[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        jpegQTables[e] = elements.get(e).longValue();
                    }
                    break;
                case JPEGDCTables:
                    jpegDCTables = new long[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        jpegDCTables[e] = elements.get(e).longValue();
                    }
                    break;
                case JPEGACTables:
                    jpegACTables = new long[elements.size()];
                    for (int e = 0; e < elements.size(); e++) {
                        jpegACTables[e] = elements.get(e).longValue();
                    }
                    break;
            }
        }
        tileWidth = (int) ((tileWidth==-1)? imageWidth : tileWidth) ;
        tilesAcrossW = (int) ((imageWidth + tileWidth - 1) / tileWidth);
        tilesDownL = (int) ((imageLength + tileLength - 1) / tileLength);
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public short getCompression() {
        return compression;
    }
                
    synchronized public byte[] getTileAsByteArray(int tileX, int tileY) throws IOException {
        return getTileAsByteArray(calculateTileByTileCord(tileX, tileY));
    }
    
    BufferedImage toPlot = null;
    JFrame debug = new JFrame(){
        @Override
        public void paint(Graphics g) {
            super.paint(g); //To change body of generated methods, choose Tools | Templates.
            ((Graphics2D)g).drawImage(toPlot, new AffineTransform(), null);
        }
    };
    
    synchronized public byte[] getTileAsByteArray(int tileIndex) throws IOException {
        long offset = tileOffsets[tileIndex];
        int numOfBytes = tileByteCounts[tileIndex];
        byte[] b = new byte[numOfBytes];
        rafle.seek(offset);
        rafle.readLittleEndian(b);
        return b;        
    }

    public int calculateTileX(int x) {
        int i = (int) ((x + tileWidth - 1) / tileWidth);
        if (i > tilesAcrossW) {
            throw new ArrayIndexOutOfBoundsException("Tile index out of bounds");
        }
        return i;
    }

    public int calculateTileY(int y) {
        int i = (int) ((y + tileLength - 1) / tileLength);
        if (i > tilesDownL) {
            throw new ArrayIndexOutOfBoundsException("Tile index out of bounds");
        }
        return i;
    }

    public int calculateTileByTileCord(int tileX, int tileY) {
        return (int) ((tilesAcrossW * tileY) + tileX);
    }
    
    public Properties getProperties(){
            Properties p = new Properties();
            p.put("ImageWidth",     Long.toString(getImageWidth()));
            p.put("ImageLength",    Long.toString(getImageLength()));
            p.put("Compression",    Long.toString(getCompression()));
            p.put("TileLength",     Long.toString(getTileLength()));
            p.put("TileWidth",      Long.toString(getTileWidth()));
            p.put("TilesAcrossW",   Long.toString(getTilesAcrossW()));
            p.put("TilesDownL",     Long.toString(getTilesDownL()));
            p.put("Photometric",    Long.toString(getPhotometric()));
            p.put("JpegProc",       Long.toString(getJpegProc()));
            if (getTileByteCounts() != null){
                StringBuilder sb = new StringBuilder();
                for (int i : getTileByteCounts()) {
                    sb.append(i).append(",");
                }
                p.put("TileByteCounts", sb.substring(0, sb.length()-1));
            }
            if (getJpegTables() != null){
                String s = DatatypeConverter.printBase64Binary(getJpegTables());                
                p.put("JpegTables", s);
            }
            if (zoomLevel != null){
                p.put("ZoomLevel", zoomLevel);
            }
        return p;
    }

    public int[] getTileByteCounts() {
        return tileByteCounts;
    }

}
