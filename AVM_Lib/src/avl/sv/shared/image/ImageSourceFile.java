package avl.sv.shared.image;

import avl.tiff.BufferingStatus;
import avl.tiff.TiffDirectory;
import avl.tiff.TiffDirectoryBuffer;
import avl.tiff.TiffFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageSourceFile extends ImageSource {

    private final ArrayList<TiffDirectory> tds;
    
    public ImageSourceFile(File imageFile) throws IOException {
        super(new ImageReference(null, imageFile.getName(), ImageID.get(imageFile)));
        tds = TiffFile.getTiffDirectories(imageFile);
        gatherDirs();
    }

    private void gatherDirs() {
        if (tds == null){
            return;
        }
        ArrayList<TiffDirectoryBuffer> directoryBuffers = new ArrayList<>();
        for (TiffDirectory td : tds) {
            Properties p = td.getProperties();
            TiffDirectoryBuffer tdb = new TiffDirectoryBuffer(p);
            byte tables[] = td.getJpegTables();
            if (tables != null) {
                try {
                    tdb.setupDecoder(tables);
                } catch (IOException ex) {
                    Logger.getLogger(ImageSourceFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            directoryBuffers.add(tdb);
        }
        super.setTiffDirectoryBuffers(directoryBuffers);
        parseDBuff();
    }

    @Override
    protected void downloadTile(TiffDirectoryBuffer dir, int i, int j)  {
        try {
            byte b[];
            int dirIdx = super.getTiffDirectoryBufferIndexOf(dir);
            b = tds.get(dirIdx).getTileAsByteArray(i, j);
            if (b != null) {
                dir.setTile(i, j, b);
                dir.setTileFechingStatus(i, j, BufferingStatus.BUFFERED);
            } else {
                dir.setTileFechingStatus(i, j, BufferingStatus.NOT_BUFFERED);
            }
            decrementAndGetActiveDownloadCount();
            startBuffering();
        } catch (IOException ex) {
            Logger.getLogger(ImageSourceFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
