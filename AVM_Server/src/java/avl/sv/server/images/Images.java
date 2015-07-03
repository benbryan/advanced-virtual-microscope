package avl.sv.server.images;

import avl.sv.shared.SessionManager;
import avl.sv.server.SessionManagerServer;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageManagerKVStore;
import avl.sv.shared.KVStoreRef;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageSourceKVStore;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImagesSourceKVStore;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "Images")

public class Images {
    
    @Resource
    private WebServiceContext wsContext;
    private final UploadAuthManager uploadAuthManager = UploadAuthManager.getInstance();
    private static SessionManagerServer sessionManager = SessionManagerServer.getInstance();
    private AVM_Session getSession(){
        return sessionManager.getSession(sessionManager.getSessionID(wsContext));
    }
    
    @WebMethod(operationName = "getImageInfo")
    public String[] getImageInfo( @WebParam(name = "ImageReferenceXML") final String imageReferenceXML) {
        AVM_Session session = getSession();
        if (session == null) { return null; }
        ImageReference imageReference = new ImageReference(imageReferenceXML);
        ImageManager imageManager = new ImageManagerKVStore(imageReference, session);
        
        ImageSourceKVStore image = new ImagesSourceKVStore(session).createImageSource(imageManager);  
        if (image == null){
            return null;
        }
        String[] props = image.getPropertiesAsStrings();
        image.close();
        return props;
    }

    @WebMethod(operationName = "getTile")
    public byte[] getTile(  @WebParam(name = "ImageReferenceXML") final String imageReferenceXML,
                            @WebParam(name = "directoryIndex") final int directoryIndex, 
                            @WebParam(name = "tileX") final int tileX, 
                            @WebParam(name = "tileY") final int tileY)  {
        AVM_Session session = getSession();
        if (session == null) { return null; }
        ImageReference imageReference = new ImageReference(imageReferenceXML);
        byte out[] = ImageSourceKVStore.getTileAsByteArray(imageReference, directoryIndex, tileX, tileY);
        return out;
    }

    @WebMethod(operationName = "getJpegTables")
    public byte[] getJpegTables( @WebParam(name = "ImageReferenceXML") final String imageReferenceXML,
                                 @WebParam(name = "directoryIndex") final int directoryIndex)  {
        AVM_Session session = getSession();
        if (session == null) { return null; }
        ImageReference imageReference = new ImageReference(imageReferenceXML);
        ImageManager imageManager = new ImageManagerKVStore(imageReference, session);
        ImageSourceKVStore image = new ImagesSourceKVStore(session).createImageSource(imageManager);  
        if (image == null){
            return null;
        }
        byte[] out = image.getJpegTables(directoryIndex);
        image.close();
        return out;
    }
        
    @WebMethod(operationName = "getImageSets")
    public String getImageSets() {
        if (KVStoreRef.getRef() == null){
            return "error: sorry, the image setver is having issues";
        }
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        return ImageManagerSet.toXML(new ImagesSourceKVStore(session).getImageSets());
    }    
      

    @WebMethod(operationName = "imagePostSetupDirectory")
    public String imagePostSetupDirectory(  @WebParam(name = "uploadToken") final String uploadToken, 
                                            @WebParam(name = "directoryIdx") final int directoryIdx, 
                                            @WebParam(name = "propertiesXML") final String propertiesXML) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        UploadAuth auth = uploadAuthManager.getAuth(uploadToken);
        if ((auth == null) || (auth.isExpired())){
            return "error: no authorization or upload session expired";
        }
        try {
            Properties props = new Properties();
            props.loadFromXML(new BufferedInputStream(new ByteArrayInputStream( propertiesXML.getBytes())));
            return auth.imageManager.setupDirectory(directoryIdx, props);
        } catch (IOException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, null, ex);
            return "error: somthing went wrong during authroized access";
        }
    }
    
    @WebMethod(operationName = "imagePostTile")
    public String imagePostTile( @WebParam(name = "uploadToken") final String uploadToken, 
                                 @WebParam(name = "directoryIdx") final int directoryIdx, 
                                 @WebParam(name = "tileX") final int tileX, 
                                 @WebParam(name = "tileY") final int tileY, 
                                 @WebParam(name = "tile") final byte tile[]) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        UploadAuth auth = uploadAuthManager.getAuth(uploadToken);
        if (auth.isExpired()){
            return "error: no authorization or upload session expired";
        }        
        if ((tileX < 0) || (tileY < 0)){
            return "error: tile idx out of bounds";
        }
        if (directoryIdx < 0){
            return "error: directory idx out of bounds";
        }
        if ((tile == null) || (tile.length == 0)){
            return "error: tile is null";
        }
        return auth.imageManager.setTile(directoryIdx, tileX, tileY, tile);

    }
    
    @WebMethod(operationName = "imagePostFinalize")
    public String imagePostFinalize( @WebParam(name = "uploadToken") final String uploadToken, 
                                     @WebParam(name = "deleteImage") final boolean deleteImage){
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        UploadAuth auth = uploadAuthManager.removeAuth(uploadToken);
        if (auth == null){
            return "error: Somthing failed during post, image should not exist on server";
        } else if (deleteImage){
            auth.imageManager.delete();
            return "Image deleted";            
        } else {
            return "Successfully added the image to the server";
        }
    }
    
    

    

    


}

