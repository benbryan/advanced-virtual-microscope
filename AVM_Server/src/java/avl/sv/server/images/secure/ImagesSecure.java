package avl.sv.server.images.secure;

import avl.sv.shared.SessionManager;
import avl.sv.shared.SessionManagerKVStore;
import avl.sv.server.SessionManagerServer;
import avl.sv.server.images.UploadAuth;
import avl.sv.server.images.UploadAuthManager;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageManagerKVStore;
import avl.sv.shared.image.ImagesSourceKVStore;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.AVM_Session;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "ImagesSecure")
public class ImagesSecure {
    
    @Resource
    private WebServiceContext wsContext;
    private static SessionManagerServer sessionManager = SessionManagerServer.getInstance();
    private AVM_Session getSession(){
        return sessionManager.getSession(sessionManager.getSessionID(wsContext));
    }
    private final UploadAuthManager uploadAuthManager = UploadAuthManager.getInstance();
    
    @WebMethod(operationName = "getOwnedSets")
    public String getOwnedSets() {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        return ImageManagerSet.toXML(new ImagesSourceKVStore(session).getOwnedImages());
    }  
    
    @WebMethod(operationName = "imagePostInit")
    public String imagePostInit( @WebParam(name = "ImageReferenceXML") final String imageReferenceXML) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        ImageReference ir = new ImageReference(imageReferenceXML);
        ImageManagerKVStore imageManager = new ImageManagerKVStore(ir, session);
        if (!imageManager.initUpload().contains("error:")){
            UploadAuth auth = new UploadAuth(session, imageManager);
            uploadAuthManager.putAuth(auth.uploadToken, auth);
            return auth.uploadToken;
        } else {
            return "error: failed to initialize post, imageset/name combo may already exist";
        }
    }
    
    @WebMethod(operationName = "deleteImage")
    public String deleteImage( @WebParam(name = "ImageReferenceXML") final String imageReferenceXML){
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        ImageReference sr = new ImageReference(imageReferenceXML);
        ImageManagerKVStore imageUpload = new ImageManagerKVStore(sr, session);
        return imageUpload.delete();              
    }

    @WebMethod(operationName = "setImageDescription")
    public String setImageDescription( @WebParam(name = "imageReferenceXML") final String imageReferenceXML,
                                       @WebParam(name = "description") final String description) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        ImageReference ir = new ImageReference(imageReferenceXML);
        ImageManagerKVStore manager = new ImageManagerKVStore(ir, session);
        return manager.setDescription(description);
    }

    @WebMethod(operationName = "getDescription")
    public String getDescription( @WebParam(name = "imageReferenceXML") final String imageReferenceXML) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        ImageReference ir = new ImageReference(imageReferenceXML);
        ImageManagerKVStore manager = new ImageManagerKVStore(ir, session);
        return manager.getDescription();
    }
}
