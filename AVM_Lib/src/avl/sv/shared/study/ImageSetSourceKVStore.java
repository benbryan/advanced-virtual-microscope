package avl.sv.shared.study;

import avl.sv.shared.KVStoreRef;
import avl.sv.shared.PermissionsSet;
import avl.sv.shared.Permissions;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerKVStore;
import avl.sv.shared.image.ImageReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.xml.bind.DatatypeConverter;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

public class ImageSetSourceKVStore {
    private final String name;
    private final int id;
    private KVStore kvstore = KVStoreRef.getRef();
    private final AVM_Session session;

    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
    
    private ImageSetSourceKVStore(AVM_Session session, String imageSetName, int id) {
        this.name = imageSetName;
        this.id = id;
        this.session = session;
    }

    public long getVersion(){
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        major.add(String.valueOf(getId()));
        ValueVersion vv = kvstore.get(Key.createKey(major));
        if (vv == null){
            kvstore.put(Key.createKey(major), Value.EMPTY_VALUE);
            vv = kvstore.get(Key.createKey(major));
        }
        return vv.getVersion().getVLSN();
    }
    
    public static ImageSetSourceKVStore get(AVM_Session session, int id){
        KVStore kvstore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        major.add(String.valueOf(id));
        ValueVersion temp = kvstore.get(Key.createKey(major));
        if (temp == null){
            return null;
        } else {
            return new ImageSetSourceKVStore(session, "", id);
        }
    }
    
    public static ImageSetSourceKVStore create(AVM_Session session, String userName, String imageSetName){
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        KVStoreRef.getRef().putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
        int id = createID();
        major.add(String.valueOf(id));
        ImageSetSourceKVStore ss = new ImageSetSourceKVStore(session, imageSetName, id);
        ss.setPermission(userName, Permissions.ADMIN); 
        return ss;
    }

    private static int createID(){
        KVStore kvstore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        major.add("toOverWrite");
        while (true){
            //TODO: this could cause things to hang, maybe. 
            int r = (int) (Math.random()*Math.pow(2, 32));
            major.set(1, String.valueOf(r));
            ValueVersion vv = kvstore.get(Key.createKey(major));
            if (vv == null){
                kvstore.put(Key.createKey(major), Value.EMPTY_VALUE);
                return r;
            }
        }
    }
    
    public Permissions getPermissions(String userName){
        ArrayList<PermissionsSet> pSets = getPermissionsSets(kvstore, userName);
        for (PermissionsSet pSet:pSets){
            if (pSet.getID() == getId()){
                return pSet.getPermission();
            }
        }
        return Permissions.DENIED;
    }
    public void setPermission( final String userName, final Permissions permission) {
        PermissionsSet newSet = new PermissionsSet(permission, getId());
        ArrayList<PermissionsSet> permissionSets = getPermissionsSets(kvstore, userName);
        boolean entryUpdated = false;
        for (int i = 0; i < permissionSets.size(); i++){
            if (permissionSets.get(i).getID() == getId()){
                if (permission == Permissions.DENIED){
                    permissionSets.remove(i);
                } else {
                    permissionSets.set(i, newSet);
                }
                entryUpdated = true;
                break;
            }
        }
        if (entryUpdated == false){
            permissionSets.add(newSet);                
        }
        setPermissionsSets( kvstore, userName, permissionSets);
        updateUserList(userName, permission);
    }
            
    public static ArrayList<PermissionsSet> getPermissionsSets(KVStore kvstore, final String userName) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(userName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("imageSet");
        ArrayList<PermissionsSet> permissionSet = new ArrayList<>();
        ValueVersion vv = kvstore.get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null)){
            return permissionSet;
        }
        String entries[] = new String(vv.getValue().getValue()).split(System.lineSeparator());
        for (String entry:entries){
            // entry should be in the format   name:permission:ID
            String[] str = entry.split(",");
            Permissions p;
            if (str.length != 3){
                continue;
            }
            try{
                p = Permissions.valueOf(str[1]);
            } catch (Exception ex){
                continue;
            } 
            permissionSet.add(new PermissionsSet(p, Integer.valueOf(str[2])));
        }
        return permissionSet;
    }  
    private static void setPermissionsSets(final KVStore kvstore, final String userName, final ArrayList<PermissionsSet> permissionSets) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(userName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("imageSet");
        StringBuilder sb = new StringBuilder();
        for (PermissionsSet permissionSet:permissionSets){
            // entry should be in the format   name:permission:ID
            String entry = permissionSet.getPermission().name() + "," + permissionSet.getID() + System.lineSeparator();
            sb.append(entry);
        }
        kvstore.put(Key.createKey(major,minor), Value.createValue(sb.toString().getBytes()));
    }  
    public static long getPermissionsSetsVersion(KVStore kvstore, final String userName) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(userName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("imageSet");
        ValueVersion vv = kvstore.get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null)){
            Value v = Value.createValue("".getBytes());
            kvstore.put(Key.createKey(major, minor), v);
            vv = kvstore.get(Key.createKey(major, minor));
        } 
        return vv.getVersion().getVLSN();
    }  
    
    public String getUserList(){   
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        major.add(String.valueOf(id));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("users");     
        ValueVersion vv = kvstore.get(Key.createKey(major, minor));
        if (vv == null){
            return "";
        } else {
            return new String(vv.getValue().getValue());
        }
    }
    private void updateUserList(String userName, Permissions permission){   
        HashSet<String> users = new HashSet<>();
        users.addAll(Arrays.asList(getUserList().split(";")));
        users.add(userName);
        if (permission.equals(Permissions.DENIED)){
            users.remove(userName);
        }
        StringBuilder sb = new StringBuilder();
        for (String user:users){
            sb.append(user).append(";");
        }
        setUserList(sb.toString());
    }
    private void setUserList(String userList){
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        kvstore.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
        major.add(String.valueOf(id));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("users");     
        kvstore.put(Key.createKey(major, minor), Value.createValue(userList.getBytes()));        
    }
    
    public void setImageSet(ArrayList<ImageManager> imageManagers) {
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        kvstore.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
        major.add(String.valueOf(getId()));
        StringBuilder sb = new StringBuilder();
        for (ImageManager imageManager:imageManagers){
            String hashStr = DatatypeConverter.printBase64Binary(imageManager.imageReference.hash);
            sb.append(imageManager.imageReference.imageSetName).append(",").append(imageManager.imageReference.imageName).append(",").append(hashStr).append(";");
        }
        kvstore.put(Key.createKey(major), Value.createValue(sb.toString().getBytes()));
    }
    public ArrayList<ImageManager> getImageSet() {
        ArrayList<String> major = new ArrayList<>();
        major.add("imageSet");
        kvstore.putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
        major.add(String.valueOf(getId()));
        ValueVersion value = kvstore.get(Key.createKey(major));
        ArrayList<ImageManager> imageManagers = new ArrayList<>();
        if ((value == null) || (value.getValue() == null)) {
            return imageManagers;
        }

        String sRef = new String(value.getValue().getValue());
        for (String sPair : sRef.split(";")) {
            String temp[] = sPair.split(",");
            if (temp.length != 3) {
                continue;
            }
            byte hash[] = DatatypeConverter.parseBase64Binary(temp[2]);
            ImageReference imageReference = new ImageReference(temp[0],temp[1],hash);
            imageManagers.add(new ImageManagerKVStore(imageReference,session));
        }
        return imageManagers;
    }
    
}
