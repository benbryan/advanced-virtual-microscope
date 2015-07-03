package avl.sv.shared.image;

import avl.sv.shared.KVStoreRef;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.AVM_Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

public class ImageManagerKVStore extends ImageManager {
        
    static {
        KVStoreRef.getRef();
    } 
    
    private final AVM_Session session;
    private boolean initialized = false;

    public ImageManagerKVStore(ImageReference imageReference, AVM_Session session) {
        super(imageReference);
        this.session = session;
    }   
    
    private ArrayList<String> getMajorPath(){
        final ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);   
        return major;
    }
      
    @Override
    public String initUpload() {
        if (exists()){
            return "error: Image already exists in database";
        }
        KVStore kv = KVStoreRef.getRef();
        final ArrayList<String> major = new ArrayList<>();
        major.add("image");
        kv.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
        major.add(imageReference.imageSetName);
        kv.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
        major.add(imageReference.imageName);
        kv.put(Key.createKey(major), Value.createValue(imageReference.hash));
        addOwnerEntry();
        initialized = true;
        return "Image initialized";
    }
    
    private void addOwnerEntry(){
        KVStore kv = KVStoreRef.getRef();
        final ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(session.username);
        final ArrayList<String> minor = new ArrayList<>();
        minor.add("Images Owned");
        kv.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
        minor.add(imageReference.imageSetName);
        kv.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
        minor.add(imageReference.imageName);
        kv.put(Key.createKey(major, minor), Value.createValue(imageReference.hash));
    }
    
    @Override
    public boolean exists( ) {
        KVStore kvstore = KVStoreRef.getRef();
        ValueVersion vv = kvstore.get(Key.createKey(getMajorPath()));
        return vv != null;
    }

    //TODO: check below statement about TileDirecotry
    /**
     * Sets up an image directory before posting tiles 
     * @param directoryIdx
     * tile directory index from TileDirectory
     * @param props
     * props from a TileDirecotry buffer
     */
    @Override
    public String setupDirectory( final int directoryIdx, 
                                final Properties props) {
        if (!initialized){
            return "error: not initialized";
        }
        KVStore kvstore = KVStoreRef.getRef();
        final ArrayList<String> major = getMajorPath();

        ArrayList<String> minor = new ArrayList<>();
        minor.add(String.valueOf(directoryIdx));
        kvstore.put(Key.createKey(major, minor), Value.EMPTY_VALUE);
        minor.add("properties");
        kvstore.put(Key.createKey(major, minor), Value.EMPTY_VALUE);
        minor.add("This string just allocates space");

        for (String propName:props.stringPropertyNames()){
            minor.set(2, propName);
            String propData = props.getProperty(propName);
            if ("JpegTables".equals(propName)){
                byte b[] = DatatypeConverter.parseBase64Binary(propData);
                kvstore.put(Key.createKey(major, minor), Value.createValue(b));
            } else {
                kvstore.put(Key.createKey(major, minor), Value.createValue(propData.getBytes()));
            }
        }
        return MessageStrings.SUCCESS;
    }
    
    @Override
    public String setTile( final int directoryIdx, 
                                 final int tileX, 
                                 final int tileY, 
                                 final byte tile[]) {
        if (!initialized){
            return "error: not initialized";
        }
        ArrayList<String> minor = new ArrayList<>();
        minor.add(String.valueOf(directoryIdx));
        minor.add(String.valueOf("tiles"));
        minor.add(String.valueOf(tileX));
        minor.add(String.valueOf(tileY));
        KVStoreRef.getRef().put(Key.createKey(getMajorPath(), minor), Value.createValue(tile));
        return MessageStrings.SUCCESS;
    }
        
    @Override
    public String delete(){
        if (!isOwner()){
            return "error: cannot delete image you don't own";
        }
        deleteImage();
        deleteOwnerInfo();
        return "image " + imageReference.imageName+ " in dataset " + imageReference.imageSetName + " was deleted from the server";  
    }
    
    private void deleteOwnerInfo(){
        KVStore kv = KVStoreRef.getRef();
        final ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(session.username);
        final ArrayList<String> minor = new ArrayList<>();
        minor.add("Images Owned");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);
        kv.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
    }
    
    private void deleteImage(){
        KVStore kv = KVStoreRef.getRef();
        final ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);
        Iterator<Key> iter = kv.storeKeysIterator(Direction.UNORDERED, 0, Key.createKey(major), null, Depth.PARENT_AND_DESCENDANTS);
        while (iter.hasNext()){
            Key key = iter.next();
            kv.multiDelete(key, null, Depth.PARENT_AND_DESCENDANTS);
        } 
    }

    @Override
    public void finished() {
        
    }
    
    public boolean isOwner(){
        KVStore kv = KVStoreRef.getRef();
        final ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(session.username);
        final ArrayList<String> minor = new ArrayList<>();
        minor.add("Images Owned");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);
        ValueVersion vv = kv.get(Key.createKey(major, minor));
        return !((vv == null) || (vv.getValue() == null) || (vv.getValue().getValue() == null));
    }
    
    private ArrayList<String> getMajorImagePath(){
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);
        return major;
    }
    
    @Override
    public String setDescription(String description) {
        if (!isOwner()){
            return "Cannot edit the description of an image you did not upload";
        }
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> minor = new ArrayList<>();
        minor.add("description");
        kv.put(Key.createKey(getMajorImagePath(), minor), Value.createValue(description.getBytes()));
        return description;
    }

    @Override
    public String getDescription() {
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> minor = new ArrayList<>();
        minor.add("description");
        ValueVersion vv = kv.get(Key.createKey(getMajorPath(), minor));
        if ((vv == null) || (vv.getValue() == null) || (vv.getValue().getValue() == null)){
            return "No description";
        } else {
            String desc = new String(vv.getValue().getValue());
            return desc;
        }
    }
    
}
