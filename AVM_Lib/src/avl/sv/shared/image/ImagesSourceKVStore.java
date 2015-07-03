package avl.sv.shared.image;

import avl.sv.shared.KVStoreRef;
import avl.sv.shared.AVM_Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.ValueVersion;

public class ImagesSourceKVStore implements ImagesSource{

    private final AVM_Session session;
    public ImagesSourceKVStore(AVM_Session session) {
        this.session = session;
    }    
    
    @Override
    public ImageSourceKVStore createImageSource(ImageManager imageManager) {
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageManager.imageReference.imageSetName);
        major.add(imageManager.imageReference.imageName);
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major));
        if ((vv == null) || (vv.getValue() == null) || (vv.getValue().getValue() == null)){
            return null;
        }
        byte[] hashInStore = vv.getValue().getValue();
        if (ImageID.hashesAreEqual(hashInStore, imageManager.imageReference.hash)) {
            return new ImageSourceKVStore(imageManager.imageReference);
        }
        return null;
    }

    @Override
    public ArrayList<ImageManagerSet> getImageSets() {
        ArrayList<ImageManagerSet> dataSets = new ArrayList<>();
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        Iterator<Key> datasetIter = KVStoreRef.getRef().storeKeysIterator(Direction.UNORDERED, 0, Key.createKey(major), null, Depth.CHILDREN_ONLY);
        while (datasetIter.hasNext()) {
            Key datasetKey = datasetIter.next();
            Iterator<KeyValueVersion> imageIter = KVStoreRef.getRef().storeIterator(Direction.UNORDERED, 0, datasetKey, null, Depth.CHILDREN_ONLY);
            while (imageIter.hasNext()){
                KeyValueVersion vv = imageIter.next();
                java.util.List<String> path = vv.getKey().getMajorPath();
                String imageName = path.get(path.size()-1);
                String imageSetName = path.get(path.size()-2);
                byte imageHash[] = vv.getValue().getValue();
                
                ImageReference imageReference = new ImageReference(imageSetName, imageName, imageHash);
                ImageManager sn = new ImageManagerKVStore(imageReference, session);
                
                boolean added = false;
                for (ImageManagerSet imageSet:dataSets){
                    if (imageSetName.equals(imageSet.getName())){
                        imageSet.add(sn);
                        added = true;
                        break;
                    }
                }
                if (added == false){
                    ImageManagerSet imageSet = new ImageManagerSet(imageSetName);
                    imageSet.add(sn);
                    dataSets.add(imageSet);
                }
            }
        }     
        return dataSets;
    }
    
    @Override
    public ArrayList<ImageManagerSet> getOwnedImages() {
        ArrayList<ImageManagerSet> dataSets = new ArrayList<>();
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(session.username);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("Images Owned");
        Iterator<Key> datasetIter = KVStoreRef.getRef().multiGetKeysIterator(Direction.FORWARD, 0, Key.createKey(major, minor), null, Depth.CHILDREN_ONLY);
        while (datasetIter.hasNext()) {
            Iterator<KeyValueVersion> imageIter = KVStoreRef.getRef().multiGetIterator(Direction.FORWARD, 0, datasetIter.next(), null, Depth.CHILDREN_ONLY);
            while (imageIter.hasNext()){
                KeyValueVersion vv = imageIter.next();
                List<String> path = vv.getKey().getMinorPath();
                String imageName = path.get(path.size()-1);
                String imageSetName = path.get(path.size()-2);
                byte imageHash[] = vv.getValue().getValue();
                               
                ImageReference imageReference = new ImageReference(imageSetName, imageName, imageHash);
                ImageManager sn = new ImageManagerKVStore(imageReference, session);
                
                boolean added = false;
                for (ImageManagerSet imageSet:dataSets){
                    if (imageSetName.equals(imageSet.getName())){
                        imageSet.add(sn);
                        added = true;
                        break;
                    }
                }
                if (added == false){
                    ImageManagerSet imageSet = new ImageManagerSet(imageSetName);
                    imageSet.add(sn);
                    dataSets.add(imageSet);
                }
            }
        }    
        return dataSets;
    }

}
