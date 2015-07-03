package avl.sv.shared.image;

import avl.sv.shared.MessageStrings;
import avl.sv.shared.Rect;
import avl.sv.shared.study.Attribute;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROI_Folder;
import avl.tiff.BufferingStatus;
import avl.tiff.TiffDirectoryBuffer;
import avl.tiff.TiffTag;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.TiledImage;

public abstract class ImageSource {

    private final ArrayList<TiffDirectoryBuffer> tdBuffs = new ArrayList<>();
    private int imageDimX, imageDimY;
    private ExecutorService pool;
    public final ImageReference imageReference;
    private Callable postBufferFillEvent;
    AtomicInteger activeDownloadCount = new AtomicInteger(0);
    final int maxActiveDownloads = Math.max(4, Runtime.getRuntime().availableProcessors());
    TreeMap<Double, TiffDirectoryBuffer> zoomMap = new TreeMap<>();

    public ImageSource(ImageReference imageReference) {
        this.imageReference = imageReference;
    }

    private ExecutorService getPool() {
        if (pool == null) {
            pool = Executors.newFixedThreadPool(maxActiveDownloads);
        }
        return pool;
    }

    public TiffDirectoryBuffer getTiffDirectoryBuffer(int index) {
        return tdBuffs.get(index);
    }

    public void close() {
        for (TiffDirectoryBuffer buff : tdBuffs) {
            buff.close();
        }
    }

    protected abstract void downloadTile(TiffDirectoryBuffer dir, int i, int j);

    public byte[] getHash() {
        return imageReference.hash;
    }

    public int getImageDimX() {
        return imageDimX;
    }

    public int getImageDimY() {
        return imageDimY;
    }

    public TreeMap<Double, TiffDirectoryBuffer> getZoomMap() {
        return zoomMap;
    }

    public void setPostBufferFillEvent(Callable postBufferFillEvent) {
        this.postBufferFillEvent = postBufferFillEvent;
    }

    protected void parseDBuff() {
        imageDimX = 0;
        imageDimY = 0;
        for (int i = 0; i < tdBuffs.size(); i++) {
            TiffDirectoryBuffer d = tdBuffs.get(i);
            imageDimX = Math.max(imageDimX, d.getImageWidth());
            imageDimY = Math.max(imageDimY, d.getImageLength());
            Double z = d.getZoomLevel();
            if ((!z.isInfinite()) && (!z.isNaN())) {
                zoomMap.put(z, d);
            }
        }
        if ((zoomMap.isEmpty()) && (tdBuffs.size() > 0)) {
            zoomMap.put(1d, tdBuffs.get(0));
        }
    }

    public void clearBuffers() {
        for (TiffDirectoryBuffer d : tdBuffs) {
            d.clearBuffers();
        }
    }

    public TiffDirectoryBuffer getThumbnailDirectoryBuffer() {
        return zoomMap.firstEntry().getValue();
    }

    public TiffDirectoryBuffer getBaseDirectoryBuffer() {
        return zoomMap.lastEntry().getValue();
    }

    public int decrementAndGetActiveDownloadCount() {
        return activeDownloadCount.decrementAndGet();
    }

    public BufferedImage getSubImage(Rectangle bounds, TiffDirectoryBuffer dir) {
        if (zoomMap.isEmpty()) {
            return null;
        }
        BufferedImage img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) img.getGraphics();

        Rect r = new Rect(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        g.translate(-r.getLx(), -r.getLy());
        Point tilesToPlot[] = findTilesRect(dir, r);

        CompletionService<FillBuffer2> completionService = new ExecutorCompletionService<>(getPool());

        int width = dir.getTileWidth();
        int height = dir.getTileLength();
        int numelToDo = 0;
        for (Point tileToPlot : tilesToPlot) {
            int w = tileToPlot.x;
            int l = tileToPlot.y;
            if (dir.getTile(w, l) == null) {
                dir.setTileFechingStatus(w, l, BufferingStatus.BUFFERING);
                try {
                    completionService.submit(new FillBuffer2(tileToPlot.x, tileToPlot.y, dir));
                } finally {
                    numelToDo++;
                }
            } else {
                g.drawImage(dir.getTile(w, l), w * width, l * height, width, height, null);
            }
        }

        while (numelToDo > 0) {
            try {
                Future<FillBuffer2> resultFuture = completionService.poll(10, TimeUnit.SECONDS);
                if (resultFuture == null) {
                    return null;
                }
                FillBuffer2 buffer = resultFuture.get();
                numelToDo--;
                int w = buffer.w;
                int l = buffer.l;
                g.drawImage(dir.getTile(w, l), w * width, l * height, width, height, null);
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return img;
    }

    public void fillBuffers(Point[] tilesToPlot, TiffDirectoryBuffer dir) {
        ArrayList<Point> temp = new ArrayList<>();
        for (Point tileToPlot : tilesToPlot) {
            int w = tileToPlot.x;
            int l = tileToPlot.y;
            temp.add(new Point(w, l));
        }
        dir.setTilesInView(temp);
        startBuffering();
    }

    protected void startBuffering() {
        if (activeDownloadCount.get() >= maxActiveDownloads) {
            return;
        }
        Iterator<TiffDirectoryBuffer> dirIt = zoomMap.values().iterator();
        while (dirIt.hasNext()) {
            TiffDirectoryBuffer dir = dirIt.next();
            ArrayList<Point> inView = dir.getTilesInView();
            if (inView == null) {
                continue;
            }
            for (Point tile : inView) {
                int w = tile.x;
                int l = tile.y;
                if (dir.getTileFechingStatus(w, l) == BufferingStatus.NOT_BUFFERED) {
                    dir.setTileFechingStatus(w, l, BufferingStatus.BUFFERING);
                    FillBuffer fbCall = new FillBuffer(w, l, dir);
                    try {
                        getPool().submit(fbCall);
                    } finally {
                        activeDownloadCount.incrementAndGet();
                    }
                    if (activeDownloadCount.get() >= maxActiveDownloads) {
                        return;
                    }
                }
            }
        }
    }

    public boolean isTiffDirectoryBuffersEmpty() {
        return tdBuffs.isEmpty();
    }

    public void setTiffDirectoryBuffers(ArrayList<TiffDirectoryBuffer> directoryBuffers) {
        for (TiffDirectoryBuffer buff : tdBuffs) {
            buff.close();
        }
        tdBuffs.clear();
        tdBuffs.addAll(directoryBuffers);
    }

    public int getTiffDirectoryBufferIndexOf(TiffDirectoryBuffer dir) {
        return tdBuffs.indexOf(dir);
    }

    private class FillBuffer2 implements Callable<FillBuffer2> {

        int w, l;
        TiffDirectoryBuffer dir;

        public FillBuffer2(int w, int l, TiffDirectoryBuffer dir) {
            this.w = w;
            this.l = l;
            this.dir = dir;
        }

        @Override
        public FillBuffer2 call() {
            try {
                downloadTile(dir, w, l);
            } catch (Exception ex) {
                dir.setTileFechingStatus(w, l, BufferingStatus.NOT_BUFFERED);
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
            return this;
        }
    }

    private class FillBuffer implements Runnable {

        int w, l;
        TiffDirectoryBuffer dir;

        public FillBuffer(int w, int l, TiffDirectoryBuffer dir) {
            this.w = w;
            this.l = l;
            this.dir = dir;
        }

        @Override
        public void run() {
            try {
                downloadTile(dir, w, l);
                if (postBufferFillEvent != null) {
                    postBufferFillEvent.call();
                }
            } catch (Exception ex) {
                dir.setTileFechingStatus(w, l, BufferingStatus.NOT_BUFFERED);
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
            startBuffering();
        }
    }

    public void paintTiles(TiffDirectoryBuffer dir, Graphics2D g, Point tilesToPlot[]) {
        if (tilesToPlot == null) {
            return;
        }
        fillBuffers(tilesToPlot, dir);
        int width = dir.getTileWidth();
        int height = dir.getTileLength();
        for (int i = 0; i < tilesToPlot.length; i++) {
            int w = tilesToPlot[i].x;
            int l = tilesToPlot[i].y;
            g.drawImage(dir.getTile(w, l), w * width, l * height, width, height, null);
        }
    }

    public BufferedImage getSubImage(Rectangle bounds) throws ImageAccessException {
        if (zoomMap.isEmpty()) {
            throw new ImageAccessException("Could not collect sub image sample");
        }
        TiffDirectoryBuffer dir = getBaseDirectoryBuffer();
        return getSubImage(bounds, dir);
    }

    public double percentBuffered(TiffDirectoryBuffer dir, Point tiles[]) {
        int count = 0;
        for (Point tile : tiles) {
            int w = tile.x;
            int l = tile.y;
            if (dir.getTileFechingStatus(w, l) == BufferingStatus.BUFFERED) {
                count++;
            }
        }
        return count / (tiles.length);
    }

    public Point[] findTilesRect(TiffDirectoryBuffer dir, Rect r) {
        int lxt = Math.max(0, r.getLx() / dir.getTileWidth());
        int lyt = Math.max(0, r.getLy() / dir.getTileLength());
        int uxt = Math.min(dir.getTilesAcrossW() - 1, r.getUx() / dir.getTileWidth());
        int uyt = Math.min(dir.getTilesDownL() - 1, r.getUy() / dir.getTileLength());

        int dx = uxt - lxt + 1;
        int dy = uyt - lyt + 1;

        if ((dx < 1) || (dy < 1)) {
            return null;
        }

        Point tilesToPlot[] = new Point[dx * dy];
        for (int i = 0; i < dx; i++) {
            for (int j = 0; j < dy; j++) {
                tilesToPlot[i * dy + j] = new Point(lxt + i, lyt + j);
            }
        }
        return tilesToPlot;
    }

    public static ImageSource getImageSourceForHash(ArrayList<ImageSource> imageSources, byte hash[]) {
        for (ImageSource imageSource : imageSources) {
            if (ImageID.hashesAreEqual(hash, imageSource.imageReference.hash)) {
                return imageSource;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImageManager) {
            ImageReference ir = (ImageReference) obj;
            return imageReference.equals(ir);
        }
        if (obj instanceof ImageSource) {
            ImageSource is = (ImageSource) obj;
            return imageReference.equals(is.imageReference);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.imageReference);
        return hash;
    }

    public String exportROI(ROI roi, File out, float jpegQuality) {
        try {
            Rectangle rec = roi.getPolygon().getBounds();
            if (rec.x < 0) {
                rec.x = 0;
            }
            if (rec.y < 0) {
                rec.y = 0;
            }
            if (rec.width >= getImageDimX()) {
                rec.width = getImageDimX();
            }
            if (rec.height >= getImageDimY()) {
                rec.height = getImageDimY();
            }
            if ((rec.width <= 0) || (rec.height <= 0)) {
                return "error: ROI's with or height equal ot 0";
            }

            if (out.getParentFile().canWrite()) {
                BufferedImage temp = getSubImage(rec);
                TiledImage img = new TiledImage(0, 0, temp.getWidth(), temp.getHeight(), 0, 0, temp.getSampleModel(), temp.getColorModel());
                img.createGraphics().drawImage(temp, null, null);
                FileOutputStream fos = new FileOutputStream(out);
                int tileSize = 256;

                TIFFEncodeParam tiffParam = new TIFFEncodeParam();
                tiffParam.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
                tiffParam.setWriteTiled(true);
                tiffParam.setTileSize(tileSize, tileSize);
                tiffParam.setLittleEndian(true);

                JPEGEncodeParam jpegParam = tiffParam.getJPEGEncodeParam();
                jpegParam.setQuality(jpegQuality);

                ROI_Folder folder;
                if (roi.getParent() != null){
                    folder = ((ROI_Folder)roi.getParent()).clone();
                } else {
                    folder  = ROI_Folder.createDefault();
                }
                folder.add((ROI) roi.clone(), true);
                Attribute boundingBoxAttribute = new Attribute();
                boundingBoxAttribute.id = 0;
                boundingBoxAttribute.name = "Annotation_BoundingBox";
                boundingBoxAttribute.value
                        = "X=" + String.valueOf(rec.x) + "|"
                        + "Y=" + String.valueOf(rec.y) + "|"
                        + "Width=" + String.valueOf(rec.width) + "|"
                        + "Height=" + String.valueOf(rec.height);
                if (folder.attributes == null) {
                    folder.attributes = new ArrayList<>();
                }
                folder.attributes.add(boundingBoxAttribute);

                Attribute imageSetName = new Attribute();
                imageSetName.id = 1;
                imageSetName.name = "ImageSetName";
                imageSetName.value = imageReference.imageSetName;
                folder.attributes.add(imageSetName);
                
                Attribute imageName = new Attribute();
                imageName.id = 2;
                imageName.name = "ImageName";
                imageName.value = imageReference.imageName;
                folder.attributes.add(imageName);
                
                Attribute imageID = new Attribute();
                imageSetName.id = 3;
                imageSetName.name = "ImageID";
                imageSetName.value = imageReference.hashString;
                folder.attributes.add(imageID);
                
                TIFFField[] extraFields = new TIFFField[1];
                extraFields[0] = new TIFFField(TiffTag.ImageDescription.getShort(), 2, 1, new String[]{folder.toXML(true)});
                tiffParam.setExtraFields(extraFields);

                TIFFImageEncoder encoder = new TIFFImageEncoder(fos, tiffParam);
                encoder.encode(img);
                fos.close();
                return MessageStrings.SUCCESS;
            }
            return "error: permission denied to write file";
        } catch (IOException | ImageAccessException ex) {
            return "error: Failed to get subimage from " + imageReference.imageSetName + "\"" + imageReference.imageName + " for export of ROI " + roi.getName();
        } catch (OutOfMemoryError ex) {
            return "error: OutOfMemoryError while attempting to get subimage from " + imageReference.imageSetName + "\"" + imageReference.imageName + " for export of ROI " + roi.getName();
        }
    }
}
