package avl.sv.shared.solution;

import avl.sv.shared.image.ImageReference;
import avl.sv.shared.KVStoreRef;
import avl.sv.shared.PermissionsSet;
import avl.sv.shared.Permissions;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.solution.xml.SolutionXML_Parser;
import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.StudySource;
import avl.sv.shared.study.StudySourceKVStore;
import static avl.sv.shared.study.StudySourceKVStore.getPermissionsSets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;
import org.xml.sax.SAXException;

public class SolutionSourceKVStore extends SolutionSource {

    private final AVM_Session avmSession;    
    
    private SolutionSourceKVStore(AVM_Session session, int id) {
        super(id);
        this.avmSession = session;
    }
        
    public static SolutionSourceKVStore get(AVM_Session session, int id) throws PermissionDenied{
        SolutionSourceKVStore ss = new SolutionSourceKVStore(session, id);
        if (ss.getPermissions().canRead()){
            return ss;
        } else {
            throw new PermissionDenied();
        }
    }
    
    public static SolutionSourceKVStore create(AVM_Session session, String solutionName) throws PermissionDenied{      
        // Create a study with same ID as solution
        StudySourceKVStore studySource = StudySourceKVStore.create(session, "");
        SolutionSourceKVStore ss = new SolutionSourceKVStore(session, studySource.studyID);
        ss.setPermissionAsAdmin(session.username, Permissions.ADMIN); 
        ss.setSolution(new Solution(solutionName));
        ss.setName(solutionName);
        return ss;
    }
            
    @Override
    public Permissions getPermissions(String userName) throws PermissionDenied {
        userName = userName.toLowerCase();
        if (userName == null || userName.isEmpty()) {
            return getPermissions();
        }
        if (userName.equals(avmSession.username)) {
            return getPermissions();
        }
        if (!getPermissions().isAdmin()) {
            throw new PermissionDenied();
        }
        ArrayList<PermissionsSet> pSets = getPermissionsSets(userName);
        for (PermissionsSet pSet : pSets) {
            if (pSet.getID() == solutionId) {
                return pSet.getPermission();
            }
        }
        return Permissions.DENIED;
    }
    
    @Override
    public String setPermissions( final String targetUsername, final Permissions permission) throws PermissionDenied{
        if (!getPermissions().isAdmin()) {
            throw new PermissionDenied();
        }
        setPermissionAsAdmin( targetUsername, permission);
        return MessageStrings.SUCCESS;
    }
    
    private void setPermissionAsAdmin(final String targetUsername, final Permissions permission) throws PermissionDenied {
        PermissionsSet newSet = new PermissionsSet(permission, solutionId);
        ArrayList<PermissionsSet> permissionSets = getPermissionsSets(targetUsername);
        boolean entryUpdated = false;
        for (int i = 0; i < permissionSets.size(); i++){
            if (permissionSets.get(i).getID() == solutionId){
                if (permission == Permissions.DENIED){
                    permissionSets.remove(i);
                } else {
                    permissionSets.set(i, newSet);
                }
                entryUpdated = true;
                break;
            }
        }
        if ((entryUpdated == false) && (!permission.equals(Permissions.DENIED))){
            permissionSets.add(newSet);                
        }
        setPermissionsSets( targetUsername, permissionSets);
        updateUserList(targetUsername, permission);
        StudySourceKVStore.get(avmSession, solutionId).setPermission(targetUsername, permission);
    }
            
    public static ArrayList<PermissionsSet> getPermissionsSets(final String targetUsername) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(targetUsername);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("solution");
        ArrayList<PermissionsSet> permissionSet = new ArrayList<>();
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null)){
            return permissionSet;
        }
        String entries[] = new String(vv.getValue().getValue()).split(System.lineSeparator());
        for (String entry:entries){
            // entry should be in the format   name:permission:ID
            String[] str = entry.split(",");
            Permissions p;
            if (str.length != 2){
                continue;
            }
            try{
                p = Permissions.valueOf(str[0]);
            } catch (Exception ex){
                continue;
            } 
            permissionSet.add(new PermissionsSet(p, Integer.valueOf(str[1])));
        }
        return permissionSet;
    }  
    private static void setPermissionsSets(final String targetUsername, final ArrayList<PermissionsSet> permissionSets) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(targetUsername);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("solution");
        StringBuilder sb = new StringBuilder();
        for (PermissionsSet permissionSet:permissionSets){
            // entry should be in the format   name:permission:ID
            String entry = permissionSet.getPermission().name() + "," + permissionSet.getID() + System.lineSeparator();
            sb.append(entry);
        }
        KVStoreRef.getRef().put(Key.createKey(major,minor), Value.createValue(sb.toString().getBytes()));
    }  
    public static long getPermissionsSetsVersion(final String targetUsername) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(targetUsername);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("solution");
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null)){
            Value v = Value.createValue("".getBytes());
            KVStoreRef.getRef().put(Key.createKey(major, minor), v);
            vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        } 
        return vv.getVersion().getVLSN();
    }  
    
    public String getUserList(){   
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("users");     
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if (vv == null){
            return "";
        } else {
            return new String(vv.getValue().getValue());
        }
    }
    private void updateUserList(String targetUsername, Permissions permission){   
        HashSet<String> users = new HashSet<>();
        users.addAll(Arrays.asList(getUserList().split(";")));
        users.add(targetUsername);
        if (permission.equals(Permissions.DENIED)){
            users.remove(targetUsername);
        }
        StringBuilder sb = new StringBuilder();
        for (String user:users){
            if (user.isEmpty()){
                continue;
            }
            sb.append(user).append(";");
        }
        setUserList(sb.toString());
    }
    private void setUserList(String userList){
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("users");     
        KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(userList.getBytes()));        
    }
    
    @Override
    public String setSolution(String xml) throws PermissionDenied {
        if (!getPermissions().canModify()) {
            throw new PermissionDenied();
        }
        if (xml.isEmpty()){
            throw new IllegalArgumentException("Solution xml should not be empty");
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        KVStoreRef.getRef().put(Key.createKey(major), Value.createValue(xml.getBytes()));
        SolutionChangeEvent solutionChagngeEvent = new SolutionChangeEvent(solutionId, SolutionChangeEvent.Type.Full, avmSession.username, xml);
        SolutionChangeLoggerKVStoreManager.addChangeEvent(solutionChagngeEvent, avmSession.sessionID);
        return MessageStrings.SUCCESS;
    }

    public String getSolutionXML() {
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        ValueVersion value = KVStoreRef.getRef().get(Key.createKey(major));
        if ((value == null) || (value.getValue() == null) || value.getValue().getValue().length == 0) {
            return SolutionXML_Writer.getXMLString(new Solution(getName()));
        }
        String xml = new String(value.getValue().getValue());
        return xml;
    }

    @Override
    public Solution getSolution() {
        try {
            String xml = getSolutionXML();
            if ((xml == null) || (xml.isEmpty()) ) {
                return null;
            }
            if (xml.startsWith("error:")){
                throw new WebServiceException(xml);
            }
            Solution solution = new SolutionXML_Parser().parse(xml);
            return solution;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SolutionSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String delete() throws PermissionDenied {        
        if (!getPermissions().canModify()){
            throw new PermissionDenied();
        }
        removeAllSolutionChangeListeners();
        setPermissions(avmSession.username, Permissions.DENIED);
        if (getUserList().length() < 2){
            ArrayList<String> major = new ArrayList<>();
            major.add("solution");
            KVStoreRef.getRef().put(Key.createKey(major), Value.EMPTY_VALUE);
            major.add(String.valueOf(solutionId));
            SortedSet<Key> keys = KVStoreRef.getRef().multiGetKeys(Key.createKey(major), null, Depth.PARENT_AND_DESCENDANTS);
            for (Key k:keys){
                KVStoreRef.getRef().delete(k);
            }
            return "Deleted";
        }
        return "Not Deleted";
    }

    @Override
    public String setDescription(String description) throws PermissionDenied {
        if (!getPermissions().canModify()) {
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("description");       
        Key key = Key.createKey(major, minor);
        KVStoreRef.getRef().put(key, Value.createValue(description.getBytes()));
        SolutionChangeEvent solutionChagngeEvent = new SolutionChangeEvent(solutionId, SolutionChangeEvent.Type.Description, avmSession.username, description);
        SolutionChangeLoggerKVStoreManager.addChangeEvent(solutionChagngeEvent, avmSession.sessionID);
        return description;
    }

    @Override
    public String getDescription() {
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("description");       
        Key key = Key.createKey(major, minor);
        ValueVersion vv = KVStoreRef.getRef().get(key);
        if ((vv == null) || (vv.getValue() == null) || (vv.getValue().getValue() == null)){
            return "No Description";
        } else {
            String desc = new String(vv.getValue().getValue());
            return desc;
        }
    }

    @Override
    public StudySource getStudySource() {
        try {
            return StudySourceKVStore.get(avmSession, solutionId);
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
   
    public static byte[] getFeatureRaw(ImageReference imageReference, String featureGeneratorClassName, String featureName, int tileDim, int tileWindowDim){
        KVStore kvStore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("features");
        minor.add("tiled");
        minor.add(String.valueOf(tileDim));
        minor.add(String.valueOf(tileWindowDim));
        minor.add(featureGeneratorClassName);
        minor.add(featureName);
        ValueVersion vv = kvStore.get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null) || vv.getValue().getValue() == null){
            return null;
        }
        return vv.getValue().getValue();
    }
     
    @Override
    public Properties getFeatures(ImageReference imageReference, int tileDim, int tileWindowDim, String featureGeneratorName, String featureNames[]) {
        Properties features = new Properties();
        for (String featureName : featureNames) {
            byte[] feature = getFeatureRaw(imageReference, featureGeneratorName, featureName, tileDim, tileWindowDim);
            if (feature != null) {
                features.setProperty(featureName, DatatypeConverter.printBase64Binary(feature));
            }
        }
        return features;
    }

    public static double[][] getFeature(ImageReference imageReference, String featureGeneratorName, String featureName, int tileDim, int tileWindowDim){
        byte[] samplesAsBytes = getFeatureRaw(imageReference, featureGeneratorName, featureName, tileDim, tileWindowDim);
        if (samplesAsBytes == null){
            return null;
        }
        KVStore kvStore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("features");
        minor.add("tiled");
        minor.add(String.valueOf(tileDim));
        minor.add(String.valueOf(tileWindowDim));
        ValueVersion vv = kvStore.get(Key.createKey(major,minor));
        if ((vv == null) || (vv.getValue() == null) || vv.getValue().getValue() == null){
            return null;
        }
        String header = new String(vv.getValue().getValue());
        String parts[] = header.split(",");
        if (parts.length != 3){
            return null;
        }
        int width = -1; int height = -1;
        for (String part:parts){
            if (part.startsWith("Width")){
                width = Integer.valueOf(part.substring(part.indexOf("=")+1));
            } else if (part.startsWith("Height")){
                height = Integer.valueOf(part.substring(part.indexOf("=")+1));                
            }
        }
        minor.add(featureGeneratorName);
        minor.add(featureName);
        if (samplesAsBytes.length != width * height * 8) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(samplesAsBytes);
        DataInputStream dis = new DataInputStream(bis);
        double feature[][] = new double[width][height];
        try {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    feature[x][y] = dis.readDouble();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SolutionSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return feature;
    }
    
    public static void setFeature(ImageReference imageReference, String featureGeneratorName, String featureName, int tileDim, int tileWindowDim, double feature[][]){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (int x = 0; x < feature.length; x++) {
            for (int y = 0; y < feature[0].length; y++) {
                try {
                    dos.writeDouble(feature[x][y]);
                } catch (IOException ex) {
                    Logger.getLogger(SolutionSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        byte samplesAsBytes[] = bos.toByteArray();
        KVStore kvStore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("image");
        major.add(imageReference.imageSetName);
        major.add(imageReference.imageName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("features");
        minor.add("tiled");
        minor.add(String.valueOf(tileDim));
        minor.add(String.valueOf(tileWindowDim));
        String header = "Width=" + String.valueOf(feature.length) + ","+"Height=" + String.valueOf(feature[0].length);
        kvStore.put(Key.createKey(major,minor), Value.createValue(header.getBytes()));
        minor.add(featureGeneratorName);
        minor.add("dummy mark");
        minor.set(minor.size()-1, featureName);
        kvStore.put(Key.createKey(major, minor), Value.createValue(samplesAsBytes));
    }

    @Override
    public SolutionSourceKVStore cloneSolution(String cloneName) throws PermissionDenied {
        SolutionSourceKVStore clone;
        clone = SolutionSourceKVStore.create(avmSession, cloneName);
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        Iterator<KeyValueVersion> iter = kv.multiGetIterator(Direction.FORWARD, 0, Key.createKey(major), null, Depth.PARENT_AND_DESCENDANTS);
        major.set(1, String.valueOf(clone.solutionId));
        while(iter.hasNext()){
            KeyValueVersion kvv = iter.next();
            kv.put(Key.createKey(major, kvv.getKey().getMinorPath()), kvv.getValue());
        }
        
        //the above renamed the solution, so rename it again
        ArrayList<String> minor = new ArrayList<>();
        minor.add("name");
        kv.put(Key.createKey(major, minor),Value.createValue(cloneName.getBytes()));
        
        StudySourceKVStore ssClone;
        try {
            ssClone = StudySourceKVStore.get(avmSession, clone.solutionId);
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionSourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        ssClone.copyStudyFrom(solutionId);
        
        return clone;
    }

    @Override
    public Permissions getPermissions() {
        ArrayList<PermissionsSet> pSets = getPermissionsSets(avmSession.username);
        for (PermissionsSet pSet : pSets) {
            if (pSet.getID() == solutionId) {
                return pSet.getPermission();
            }
        }
        return Permissions.DENIED;
    }

    @Override
    public String generateInDatabase(ImageReference imageReference, int tileDim, int tileWindowDim, String featureGeneratorClassName, String[] featureNames) {
        return "Not supported yet.";
    }

    @Override
    public String getUsers() throws PermissionDenied {
        if (!getPermissions().isAdmin()) {
            throw new PermissionDenied();
        }
        return getUserList();
    }

    @Override
    public void addSolutionChangeListener(SolutionChangeListener listener) {
        if (!getPermissions().canRead()){
            return;
        }
        SolutionChangeLoggerKVStoreManager.addChangeLogger(solutionId, avmSession, listener);
    }

    @Override
    public void removeSolutionChangeListener(SolutionChangeListener listener) {
        SolutionChangeLoggerKVStoreManager.remove(listener);   
    }

    @Override
    public void removeAllSolutionChangeListeners() {
        SolutionChangeLoggerKVStoreManager.removeAllListeners(solutionId, avmSession);
    }

    @Override
    public void close() {
        removeAllSolutionChangeListeners();
    }

    private String name = null;
    @Override
    public String getName() {
        if (name == null){
            ArrayList<String> major = new ArrayList<>();
            major.add("solution");
            major.add(String.valueOf(solutionId));
            ArrayList<String> minor = new ArrayList<>();
            minor.add("name");
            ValueVersion temp = KVStoreRef.getRef().get(Key.createKey(major, minor));
            if ((temp == null) || (temp.getValue() == null) || (temp.getValue().getValue() == null)){
                name = "Failed to get name"; 
            } else {
                name = new String(temp.getValue().getValue());
            }
        }
        return name;
    }

    @Override
    public void setName(String name) throws PermissionDenied  {
        if (!getPermissions().canModify()){
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("solution");
        major.add(String.valueOf(solutionId));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("name");
        KVStoreRef.getRef().put(Key.createKey(major, minor),Value.createValue(name.getBytes()));
        this.name = name;
        setUserObject(name);
    }

    @Override
    public void setNameQuiet(String name) {
        this.name = name;
        setUserObject(name);
    }
    
    
}
