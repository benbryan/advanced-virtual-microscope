package avl.sv.server.images;

import avl.sv.shared.AVM_Properties;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageManager;

import java.util.Date;

public class UploadAuth{
    public final String uploadToken;
    private final Date revokerAfter;
    public final AVM_Session session;
    final ImageManager imageManager;
    public UploadAuth( AVM_Session session, ImageManager imageManager) {
        this.uploadToken = createUploadToken();
        this.revokerAfter = new Date(new Date().getTime()+Integer.parseInt(AVM_Properties.getProperty(AVM_Properties.UPLOAD_SESSION_TIMEOUT_MINUTES))*60*1000);
        this.session = session;
        this.imageManager = imageManager;
    }

    public boolean isExpired(){
        return revokerAfter.before(new Date());
    }
    
    private String createUploadToken() {
        return Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE))
                + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE))
                + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE))
                + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE));
    }    
}