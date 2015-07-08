package avl.sv.shared.study;

import avl.sv.shared.image.ImageReference;
import avl.sv.shared.KVStoreRef;
import avl.sv.shared.PermissionsSet;
import avl.sv.shared.Permissions;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImageID;
import avl.sv.shared.image.ImageManagerKVStore;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.image.ImagesSourceKVStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;
import org.xml.sax.SAXException;

public class StudySourceKVStore extends StudySource {

    private String name = null;

    protected StudySourceKVStore(AVM_Session avmSession, int id) {
        super(id, avmSession);
    }

    public static StudySourceKVStore create(AVM_Session session, String studyName) {
        KVStore kv = KVStoreRef.getRef();
        int id = createID();
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(id));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("name");
        kv.put(Key.createKey(major, minor), Value.createValue(studyName.getBytes()));
        StudySourceKVStore ss = new StudySourceKVStore(session, id);
        ss.setPermissionAsAdmin(session.username, Permissions.ADMIN);
        return ss;
    }

    private static int createID() {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(""); // this gets replaced below
        while (true) {
            //TODO: this could cause things to hang, maybe. 
            int r = (int) (Math.random() * Math.pow(2, 32));
            major.set(1, String.valueOf(r));
            Version v = KVStoreRef.getRef().putIfAbsent(Key.createKey(major), Value.EMPTY_VALUE);
            if (v != null) {
                return r;
            }
        }
    }

    public static StudySourceKVStore get(AVM_Session session, int id) throws PermissionDenied {
        StudySourceKVStore studySource = new StudySourceKVStore(session, id);
        if (studySource.getPermissions().canRead()) {
            return studySource;
        } else {
            throw new PermissionDenied();
        }
    }

    @Override
    public String getName() {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ValueVersion temp = KVStoreRef.getRef().get(Key.createKey(major));
        if (temp == null) {
            return null;
        } else {
            ArrayList<String> minor = new ArrayList<>();
            minor.add("name");
            temp = KVStoreRef.getRef().get(Key.createKey(major, minor));
            String studyName;
            if (temp != null) {
                studyName = new String(temp.getValue().getValue());
            } else {
                studyName = "failed to get solution name";
            }
            return studyName;
        }
    }

    public static ArrayList<PermissionsSet> getPermissionsSets(final String userName) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(userName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("study");
        ArrayList<PermissionsSet> permissionSet = new ArrayList<>();
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null)) {
            return permissionSet;
        }
        String entries[] = new String(vv.getValue().getValue()).split(System.lineSeparator());
        for (String entry : entries) {
            // entry should be in the format   permission:ID
            String[] str = entry.split(",");
            Permissions p;
            if (str.length != 2) {
                continue;
            }
            try {
                p = Permissions.valueOf(str[0]);
            } catch (Exception ex) {
                continue;
            }
            permissionSet.add(new PermissionsSet(p, Integer.valueOf(str[1])));
        }
        return permissionSet;
    }

    private static void setPermissionsSets(final String userName, final ArrayList<PermissionsSet> permissionSets) {
        ArrayList<String> major = new ArrayList<>();
        major.add("user");
        major.add(userName);
        ArrayList<String> minor = new ArrayList<>();
        minor.add("permissions");
        minor.add("study");
        StringBuilder sb = new StringBuilder();
        for (PermissionsSet permissionSet : permissionSets) {
            // entry should be in the format   name:permission:ID
            String entry = permissionSet.getPermission().name() + "," + permissionSet.getID() + System.lineSeparator();
            sb.append(entry);
        }
        KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(sb.toString().getBytes()));
    }

    private void updateUserList(String userName, Permissions permission) {
        HashSet<String> users = new HashSet<>();
        users.addAll(Arrays.asList(getUsers().split(";")));
        users.add(userName);
        if (permission.equals(Permissions.DENIED)) {
            users.remove(userName);
        }
        StringBuilder sb = new StringBuilder();
        for (String user : users) {
            sb.append(user).append(";");
        }
        setUserList(sb.toString());
    }

    private void setUserList(String userList) {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("users");
        KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(userList.getBytes()));
    }

    public ArrayList<ImageManagerSet> getImageManagerSets() {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");

        ArrayList<ImageManagerSet> imageManagers = new ArrayList<>();
        Iterator<Key> datasetIter = KVStoreRef.getRef().multiGetKeysIterator(Direction.FORWARD, 0, Key.createKey(major, minor), null, Depth.CHILDREN_ONLY);
        while (datasetIter.hasNext()) {
            Key datasetKey = datasetIter.next();
            Iterator<KeyValueVersion> imageIter = KVStoreRef.getRef().multiGetIterator(Direction.FORWARD, 0, datasetKey, null, Depth.CHILDREN_ONLY);
            while (imageIter.hasNext()) {
                KeyValueVersion vv = imageIter.next();
                java.util.List<String> path = vv.getKey().getMinorPath();
                String imageName = path.get(path.size() - 1);
                String datasetName = path.get(path.size() - 2);
                byte imageHash[] = vv.getValue().getValue();

                ImageReference imageReference = new ImageReference(datasetName, imageName, imageHash);
                ImageManager imageManager = new ImageManagerKVStore(imageReference, avmSession);
                boolean added = false;
                for (ImageManagerSet dataSet : imageManagers) {
                    if (datasetName.equals(dataSet.getName())) {
                        dataSet.add(imageManager);
                        added = true;
                        break;
                    }
                }
                if (added == false) {
                    ImageManagerSet dsn = new ImageManagerSet(datasetName);
                    dsn.add(imageManager);
                    imageManagers.add(dsn);
                }
            }
        }
        return imageManagers;
    }

    @Override
    public String getAnnotationSetXML(ImageReference imageReference) {
        byte[] hash = imageReference.hash;
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);

        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if (vv == null) {
            return "Image does not exist";
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return "ImageID mismatch";
            }
        }

        SortedMap<Key, ValueVersion> annoMap = KVStoreRef.getRef().multiGet(Key.createKey(major, minor), null, Depth.CHILDREN_ONLY);
        AnnotationSet annoSet = new AnnotationSet(imageReference);
        for (Iterator<Key> annIter = annoMap.keySet().iterator(); annIter.hasNext();) {
            try {
                Key annoKey = annIter.next();
                ValueVersion annoVV = annoMap.get(annoKey);
                String annoXML = new String(annoVV.getValue().getValue());
                ROI_Folder folder = ROI_Folder.parse(annoXML);
                if (folder == null) {
                    continue;
                }
                for (ROI roi : folder.getROIs()) {
                    // folder should not include any rois at this point
                    folder.remove(roi, true);
                }
                annoSet.add(folder, true);

                SortedMap<Key, ValueVersion> roiMap = KVStoreRef.getRef().multiGet(annoKey, null, Depth.CHILDREN_ONLY);
                for (Key roiKey : roiMap.keySet()) {
                    ValueVersion roiVV = roiMap.get(roiKey);
                    String roiXML = new String(roiVV.getValue().getValue());
                    ROI roi = ROI.parse(roiXML);
                    folder.add(roi, true);
                }

            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String xml = annoSet.toXML();
        return xml;
    }

    public String getROIXML(ImageReference imageReference, long annoID, long roiID) {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);

        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        byte[] hash = imageReference.hash;
        if (vv == null) {
            KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(hash));
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return null;
            }
        }

        minor.add(String.valueOf(annoID));
        minor.add(String.valueOf(roiID));
        ValueVersion roiVV = KVStoreRef.getRef().get(Key.createKey(major, minor));
        try {
            String roiXML = new String(roiVV.getValue().getValue());
            return roiXML;
        } catch (Exception ex) {
            Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void setROIFolderXML(ImageReference imageReference, String folderXML, long folderID) {
        ROI_Folder folder;
        try {
            folder = ROI_Folder.parse(folderXML);
            for (ROI roi : folder.getROIs()) {
                folder.remove(roi, true);
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        byte[] hash = imageReference.hash;
        if (vv == null) {
            return;
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return;
            }
        }

        minor.add(String.valueOf(folderID));
        folderXML = folder.toXML(false);
        KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(folderXML.getBytes()));
    }

    public void setROIXML(ImageReference imageReference, long folderID, long roiID, String roiXML) {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        KVStoreRef.getRef().put(Key.createKey(major), Value.EMPTY_VALUE); // to replace verion
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        byte[] hash = imageReference.hash;
        if (vv == null) {
            KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(hash));
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return;
            }
        }

        minor.add(String.valueOf(folderID));
        minor.add(String.valueOf(roiID));
        KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(roiXML.getBytes()));
    }

    @Override
    public void addImage(ImageManager imageManager) throws PermissionDenied {
        ImageReference imageReference = imageManager.imageReference;
        addImage(imageReference);
        super.addImage(imageManager);
    }

    public void addImage(ImageReference imageReference) throws PermissionDenied {
        if (!getPermissions().canModify()){
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        KVStoreRef.getRef().put(Key.createKey(major), Value.EMPTY_VALUE); // to replace verion
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        KVStoreRef.getRef().putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
        minor.add(imageReference.imageSetName);
        KVStoreRef.getRef().putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
        minor.add(imageReference.imageName);
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        byte[] hash = imageReference.hash;
        if (vv == null) {
            KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(hash));
        } else {
//            byte[] hashInStore = vv.getValue().getValue();
//            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
//                return MessageStrings.HASH_MISMATCH;
//            }
        }
    }

    @Override
    public void removeImage(ImageReference imageReference) throws PermissionDenied {
        if (!getPermissions().canModify()){
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        KVStoreRef.getRef().put(Key.createKey(major), Value.EMPTY_VALUE); // to replace verion
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);
        Key key = Key.createKey(major, minor);
        SortedSet<Key> keys = KVStoreRef.getRef().multiGetKeys(key, null, Depth.PARENT_AND_DESCENDANTS);
        for (Key k : keys) {
            KVStoreRef.getRef().delete(k);
        }
        super.removeImage(imageReference);
    }

    @Override
    public String setDescription(String description) throws PermissionDenied {
        if (!getPermissions().canModify()) {
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("description");
        Key key = Key.createKey(major, minor);
        KVStoreRef.getRef().put(key, Value.createValue(description.getBytes()));
        return description;
    }

    @Override
    public String getDescription() {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("description");
        Key key = Key.createKey(major, minor);
        ValueVersion vv = KVStoreRef.getRef().get(key);
        if ((vv == null) || (vv.getValue() == null) || (vv.getValue().getValue() == null)) {
            return "No Description";
        } else {
            String desc = new String(vv.getValue().getValue());
            return desc;
        }
    }

    @Override
    public String delete() throws PermissionDenied {
        if (!getPermissions().canModify()) {
            throw new PermissionDenied();
        }
        setPermission(avmSession.username, Permissions.DENIED);
        if (getUsers().length() < 1) {
            ArrayList<String> major = new ArrayList<>();
            major.add("study");
            KVStoreRef.getRef().put(Key.createKey(major), Value.EMPTY_VALUE);
            major.add(String.valueOf(studyID));

            SortedSet<Key> keys = KVStoreRef.getRef().multiGetKeys(Key.createKey(major), null, Depth.PARENT_AND_DESCENDANTS);
            for (Key k : keys) {
                KVStoreRef.getRef().delete(k);
            }
            return "Deleted";
        }
        return "Not deleted. Other users still have access";
    }

    public void copyStudyFrom(int sourceStudyID) {
        KVStore kv = KVStoreRef.getRef();

        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(sourceStudyID));
        Iterator<KeyValueVersion> iter = kv.multiGetIterator(Direction.FORWARD, 0, Key.createKey(major), null, Depth.DESCENDANTS_ONLY);
        major.set(1, String.valueOf(studyID));
        while (iter.hasNext()) {
            KeyValueVersion kvv = iter.next();
            kv.put(Key.createKey(major, kvv.getKey().getMinorPath()), kvv.getValue());
        }
    }

    @Override
    public ImagesSource getImagesSource() {
        return new ImagesSourceKVStore(avmSession);
    }

    public String getROIFolderXML(ImageReference imageReference, long folderID, boolean includeROIs) {
        byte[] hash = imageReference.hash;
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);

        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if (vv == null) {
            KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(hash));
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return null;
            }
        }
        try {
            minor.add(String.valueOf(folderID));
            Key folderKey = Key.createKey(major, minor);
            ValueVersion folderVV = KVStoreRef.getRef().get(folderKey);
            if ((folderVV == null) || (folderVV.getValue() == null) || (folderVV.getValue().getValue() == null)) {
                return "error: failed to get folder";
            }
            String folderXML = new String(folderVV.getValue().getValue());
            ROI_Folder folder = ROI_Folder.parse(folderXML);
            for (ROI roi : folder.getROIs()) {
                // there should not be any rois in the folder at this point
                folder.remove(roi, true);
            }
            if (includeROIs) {
                try {
                    SortedMap<Key, ValueVersion> roiMap = KVStoreRef.getRef().multiGet(folderKey, null, Depth.CHILDREN_ONLY);
                    for (Key roiKey : roiMap.keySet()) {
                        ValueVersion roiVV = roiMap.get(roiKey);
                        String roiXML = new String(roiVV.getValue().getValue());
                        ROI roi = ROI.parse(roiXML);
                        folder.add(roi, true);
                    }
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                    Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String xml = folder.toXML(includeROIs);
            return xml;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
            return "error: failed to get folder";
        }
    }

    @Override
    public StudySourceKVStore cloneStudy(String cloneName) {
        StudySourceKVStore clone = create(avmSession, cloneName);
        clone.copyStudyFrom(studyID);
        try {
            clone.setName(cloneName);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return clone;
    }

    @Override
    public String getImageReferenceSetsXML() {
        ArrayList<ImageManagerSet> dataSets = getImageManagerSets();
        return ImageManagerSet.toXML(dataSets);
    }

    @Override
    public String setName(String name) throws PermissionDenied {
        if (!getPermissions().canRead()) {
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("name");
        KVStoreRef.getRef().put(Key.createKey(major, minor), Value.createValue(name.getBytes()));
        return name;
        
    }

    @Override
    public String getUsers() {
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("users");
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if (vv == null) {
            return "";
        } else {
            return new String(vv.getValue().getValue());
        }
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
            if (pSet.getID() == studyID) {
                return pSet.getPermission();
            }
        }
        return Permissions.DENIED;
    }

    @Override
    public Permissions getPermissions() {
        ArrayList<PermissionsSet> pSets = getPermissionsSets(avmSession.username);
        for (PermissionsSet pSet : pSets) {
            if (pSet.getID() == studyID) {
                return pSet.getPermission();
            }
        }
        return Permissions.DENIED;
    }

    private String setPermissionAsAdmin(final String targetUsername, final Permissions permission) {
        PermissionsSet newSet = new PermissionsSet(permission, studyID);
        ArrayList<PermissionsSet> permissionSets = getPermissionsSets(targetUsername);
        boolean entryUpdated = false;
        for (int i = 0; i < permissionSets.size(); i++) {
            if (permissionSets.get(i).getID() == studyID) {
                if (permission == Permissions.DENIED) {
                    permissionSets.remove(i);
                } else {
                    permissionSets.set(i, newSet);
                }
                entryUpdated = true;
                break;
            }
        }
        if (entryUpdated == false) {
            permissionSets.add(newSet);
        }
        setPermissionsSets(targetUsername, permissionSets);
        updateUserList(targetUsername, permission);
        return "Permission set";
    }

    @Override
    public String setPermission(final String targetUsername, final Permissions permission) throws PermissionDenied {
        if (!getPermissions().isAdmin()) {
            throw new PermissionDenied();
        }
        return setPermissionAsAdmin(targetUsername, permission);
    }

    @Override
    public void clearROI_Folder(ImageReference imageReference, long folderID) throws PermissionDenied {
        if (!getPermissions().canModify()) {
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);

        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        byte[] hash = imageReference.hash;
        if (vv == null) {
            return;
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return;
            }
        }

        minor.add(String.valueOf(folderID));
        Key annoKey = Key.createKey(major, minor);
        KVStoreRef.getRef().multiDelete(annoKey, null, Depth.PARENT_AND_DESCENDANTS);
    }

    @Override
    public void clearROI(ImageReference imageReference, long folderID, long roiID) throws PermissionDenied {
        if (!getPermissions().canModify()) {
            throw new PermissionDenied();
        }
        ArrayList<String> major = new ArrayList<>();
        major.add("study");
        major.add(String.valueOf(studyID));
        KVStoreRef.getRef().put(Key.createKey(major), Value.EMPTY_VALUE); // to replace verion
        ArrayList<String> minor = new ArrayList<>();
        minor.add("annotations");
        minor.add(imageReference.imageSetName);
        minor.add(imageReference.imageName);
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        byte[] hash = imageReference.hash;
        if (vv == null) {
            return ;
        } else {
            byte[] hashInStore = vv.getValue().getValue();
            if (!ImageID.hashesAreEqual(hash, hashInStore)) {
                return;
            }
        }

        minor.add(String.valueOf(folderID));
        minor.add(String.valueOf(roiID));
        KVStoreRef.getRef().delete(Key.createKey(major, minor));
    }

    @Override
    public ImageManager getImageManager(ImageReference imageReference) {
        return new ImageManagerKVStore(imageReference, avmSession);
    }

    @Override
    public void addStudyChangeListener(ImageReference imageReference, StudyChangeListener listener) {
        if (!getPermissions().canRead()) {
            return;
        }
        StudyChangeLoggerKVStoreManager.addChangeLogger(studyID, imageReference, avmSession, listener);
    }

    @Override
    public void removeStudyChangeListener(StudyChangeListener listener) {
        StudyChangeLoggerKVStoreManager.remove(listener);
    }

    @Override
    public void removeAllStudyChangeListeners() {
        StudyChangeLoggerKVStoreManager.removeAllListeners(studyID, avmSession);
    }

    @Override
    public void addChanges(StudyChangeEvent event) {
        try {
            ImageReference imageReference = event.imageReference;
            long roiID = event.roiID;
            long folderID = event.folderID;
            String eventData = event.eventData;
            switch (event.type) {
                case Delete:
                    if (event.roiID <= 0) {
                        clearROI_Folder(imageReference, folderID);
                    } else {
                        clearROI(imageReference, folderID, roiID);
                    }
                    break;
                case Update:
                    if (event.roiID <= 0) {
                        setROIFolderXML(imageReference, eventData, folderID);
                    } else {
                        setROIXML(imageReference, folderID, roiID, eventData);
                    }
                    break;
                case AddImage:
                    addImage(imageReference);
                    break;
                case RemoveImage:
                    removeImage(imageReference);
                    break;
            }
            event.posted = true;
            StudyChangeLoggerKVStoreManager.addChangeEvent(event);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudySourceKVStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
