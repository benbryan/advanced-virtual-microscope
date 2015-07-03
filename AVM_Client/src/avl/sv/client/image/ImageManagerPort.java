package avl.sv.client.image;

import avl.sv.client.MessagesDefault;
import avl.sv.server.images.Images;
import avl.sv.server.images.secure.ImagesSecure;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageManagerPort extends ImageManager {

    final Images portUnsecure;
    final ImagesSecure portSecure;
    String uploadToken = null;
    private boolean initialized = false;

    public ImageManagerPort(ImageReference imageReference, Images portUnsecure,  ImagesSecure portSecure) {
        super(imageReference);
        this.portUnsecure = portUnsecure;
        this.portSecure = portSecure;
    }
    
    @Override
    public String setDescription(String description){
        return portSecure.setImageDescription(imageReference.toXML(), description);
    }
    
    @Override
    final public String initUpload() {
        if (uploadToken == null) {
            uploadToken = portSecure.imagePostInit(imageReference.toXML());
            if (uploadToken.contains(MessageStrings.SESSION_EXPIRED)){
                MessagesDefault.sessionExpired();
            }
        }
        return uploadToken;
    }

    @Override
    public boolean exists() {
        return uploadToken!=null;
    }

    @Override
    public String setupDirectory(int directoryIdx, Properties props) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            props.storeToXML(bos, "");
        } catch (IOException ex) {
            Logger.getLogger(ImageManagerPort.class.getName()).log(Level.SEVERE, null, ex);
            return "error: failed to read properties";
        }
        return portUnsecure.imagePostSetupDirectory(uploadToken, directoryIdx, bos.toString());
    }

    @Override
    public String setTile(int directoryIdx, int tileX, int tileY, byte[] tile) {
        return portUnsecure.imagePostTile(uploadToken, directoryIdx, tileX, tileY, tile);
    }

    @Override
    public String delete() {
        String result = portSecure.deleteImage(imageReference.toXML());
        if (result.contains(MessageStrings.SESSION_EXPIRED)){
            MessagesDefault.sessionExpired();
        }
        return result;
    }

    @Override
    public void finished() {
        portUnsecure.imagePostFinalize(uploadToken, false);
    }

    @Override
    public String getDescription() {
        return portSecure.getDescription(imageReference.toXML());
    }
    
}
