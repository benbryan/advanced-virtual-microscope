package avl.sv.client.image;

import avl.sv.server.images.Images;
import avl.sv.shared.image.ImageReference;

import avl.sv.shared.image.ImageSource;
import avl.tiff.BufferingStatus;
import avl.tiff.TiffDirectoryBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.WebServiceException;

public class ImageSourcePort extends ImageSource {
    private final Images imagesPort;
    
    private String imageReferenceXML;
    ImageSourcePort(Images imagesPort, ImageReference imageReference) {
        super(imageReference);
        this.imageReferenceXML = imageReference.toXML();
        this.imagesPort = imagesPort;

        List<String> ss;
        try {
            try {
                ss = imagesPort.getImageInfo(imageReferenceXML);
            } catch (WebServiceException ex){
                return;
            }
            ArrayList<TiffDirectoryBuffer> directoryBuffers = new ArrayList<>();
            for (int dirIdx = 0; dirIdx < ss.size(); dirIdx++) {
                String s = ss.get(dirIdx);
                Properties p = new Properties();
                ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
                p.loadFromXML(is);
                TiffDirectoryBuffer tdb = new TiffDirectoryBuffer(p);
                byte tables[] = imagesPort.getJpegTables(imageReferenceXML, dirIdx);
                if (tables != null) {
                    try {
                        tdb.setupDecoder(tables);
                    } catch (IOException ex) {
                        Logger.getLogger(ImageSourcePort.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                directoryBuffers.add(tdb);
            }
            setTiffDirectoryBuffers(directoryBuffers);
        } catch (IOException ex) {
            Logger.getLogger(ImageSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
        parseDBuff();
    }

    @Override
    protected void downloadTile(TiffDirectoryBuffer dir, int i, int j) {
        try {
            int dirIdx = super.getTiffDirectoryBufferIndexOf(dir);
            byte b[] = imagesPort.getTile(imageReferenceXML, dirIdx, i, j);
            if (b != null) {
                dir.setTile(i, j, b);
            } else {
                dir.setTileFechingStatus(i, j, BufferingStatus.NOT_BUFFERED);
            }
        } catch (WebServiceException ex) {
        } finally {
            decrementAndGetActiveDownloadCount();
            startBuffering();
        }
    }

}
