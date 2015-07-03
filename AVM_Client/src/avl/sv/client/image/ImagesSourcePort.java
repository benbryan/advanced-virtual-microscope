package avl.sv.client.image;

import avl.sv.shared.image.ImagesSource;
import avl.sv.server.images.Images;
import avl.sv.server.images.secure.ImagesSecure;
import avl.sv.shared.image.ImageManager;

import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageReferenceSet;
import avl.sv.shared.image.ImageSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class ImagesSourcePort implements ImagesSource{
    final public Images imagesPort;
    final public ImagesSecure imagesSecurePort;

    public ImagesSourcePort(Images imagesPort, ImagesSecure imagesSecurePort) {
        this.imagesPort = imagesPort;
        this.imagesSecurePort = imagesSecurePort;
    }
    
    @Override
    public ArrayList<ImageManagerSet> getImageSets() {
        String xml;
        try {
            xml = imagesPort.getImageSets();
        } catch (WebServiceException ex) {
            return null;
        }
        if (xml == null || xml.startsWith("error:")) {
            return null;
        }
        ArrayList<ImageReferenceSet> imageSetsNew = ImageReferenceSet.parse(xml);
        return convert(imageSetsNew);
    }

    public ArrayList<ImageManagerSet> convert(ArrayList<ImageReferenceSet> imageReferenceSets){
        ArrayList<ImageManagerSet> imageManagerSets = new ArrayList<>();
        for (ImageReferenceSet imageReferenceSet:imageReferenceSets){
            ImageManagerSet imageManagerSet = new ImageManagerSet(imageReferenceSet.getName());
            for (ImageReference imageReference:imageReferenceSet.getImageReferenceSet()){
                imageManagerSet.add(new ImageManagerPort(imageReference, imagesPort, imagesSecurePort));
            }
            imageManagerSets.add(imageManagerSet);
        }
        return imageManagerSets;
    }

    @Override
    public ArrayList<ImageManagerSet> getOwnedImages() {
        String xml;
        try {
            xml = imagesSecurePort.getOwnedSets();
        } catch (WebServiceException ex) {
            return null;
        }
        if (xml == null || xml.startsWith("error:")) {
            return null;
        }
        ArrayList<ImageReferenceSet> imageSetsNew = ImageReferenceSet.parse(xml);
        
        
        return convert(imageSetsNew);
    }
    
    @Override
    public ImageSource createImageSource(ImageManager imageManager) {
        return new ImageSourcePort(imagesPort, imageManager.imageReference);
    }

}
