package avl.sv.shared.study;

import avl.sv.shared.AVM_Session;
import avl.sv.shared.AVM_Source;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.Permissions;
import avl.sv.shared.image.ImageID;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.MutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

abstract public class StudySource extends AVM_Source {

    public final int studyID;
    protected final AVM_Session avmSession;
    private ArrayList<StudyChangeListener> studyChangeListeners;
    ArrayList<TreeTableModelStudy> annoSetModels = new ArrayList<>();
    
    abstract public String getName();
    abstract public String setName(String name) throws PermissionDenied;
    
    public StudySource(final int id, AVM_Session avmSession) {
        children = new Vector();
        this.studyID = id;
        this.avmSession = avmSession;
    }
    
    @Override
    public String toString() {
        return getName();
    }    

    public void addStudyChangeListener(StudyChangeListener listener){
        if (studyChangeListeners==null){
            studyChangeListeners = new ArrayList<>();
        }
        studyChangeListeners.add(listener);
    }

    public void removeStudyChangedListener(StudyChangeListener listener){
        if (studyChangeListeners==null){
            return;
        }
        studyChangeListeners.remove(listener);
    }
    
    final public AnnotationSet getAnnotationSet(final ImageReference imageReference)  {
        return getAnnotationSetModel(imageReference).getAnnotationSet();
    }

    private final StudyChangeListener changeListenerPrivate = (StudyChangeEvent event) -> {
        applyChanges(event);
        if (studyChangeListeners == null) {
            return;
        }
        for (StudyChangeListener studyChangeListener:studyChangeListeners){
            studyChangeListener.studyChanged(event);
        }
    };
    
    synchronized public final void applyChanges(StudyChangeEvent changeEvent)  {
//        if (true){
//            return;
//        }
        TreeTableModelStudy model = null;
        for (TreeTableModelStudy temp:annoSetModels){
            if (temp.getAnnotationSet().imageReference.equals(changeEvent.imageReference)){
                model = temp;
                break;
            }
        }
        if (model == null){
            return;
        }
        AnnotationSet annoSet = model.getAnnotationSet();
        try {
            switch (changeEvent.type) {
                case Delete:
                    for (ROI_Folder folder : annoSet.getROI_Folders()) {
                        if (folder.id == changeEvent.folderID) {
                            if (changeEvent.roiID <= 0) {
                                model.removeNodeFromParent(folder);
                                return;
                            } else {
                                for (ROI roi : folder.getROIs()) {
                                    if (roi.id == changeEvent.roiID) {
                                        model.removeNodeFromParent(roi);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case Update:
                    if (changeEvent.roiID <= 0) {
                        // ROI not modified
                        ROI_Folder newFolder = ROI_Folder.parse(changeEvent.eventData);
                        for (ROI_Folder folder : model.getAnnotationSet().getROI_Folders()) {
                            if (folder.id == newFolder.id) {
                                for (ROI oldROI:folder.getROIs()){
                                    boolean exists = false;
                                    for (ROI newROI:newFolder.getROIs()){
                                        if (oldROI.id == newROI.id ){
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists){
                                        newFolder.add(oldROI);
                                    }
                                }
                                int idx = model.getIndexOfChild(annoSet, folder);
                                model.removeNodeFromParent(folder);
                                model.insertNodeInto(newFolder, annoSet, idx);
                                return;
                            }
                        }
                        model.insertNodeInto(newFolder, annoSet, annoSet.getChildCount());
                    } else {
                        ROI newROI = ROI.parse(changeEvent.eventData);
                        for (ROI_Folder folder : annoSet.getROI_Folders()) {
                            if (folder.id == changeEvent.folderID) {
                                for (ROI roi : folder.getROIs()) {
                                    if (roi.id == newROI.id) {
                                        int idx = model.getIndexOfChild(folder, roi);
                                        model.removeNodeFromParent(roi);
                                        model.insertNodeInto(newROI, folder, idx);
                                        return;
                                    }
                                }
                                model.insertNodeInto(newROI, folder, folder.getROIs().size());
                                return;
                            }
                        }
                    }
                    break;
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addImage(ImageManager imageManager)  throws PermissionDenied {
        for (Object object:children) {
            if (object instanceof ImageManagerSet){
                ImageManagerSet imageManagerSet = (ImageManagerSet) object;
                if (imageManagerSet.getName().equals(imageManager.imageReference.imageSetName)) {
                    imageManagerSet.add((MutableTreeNode) imageManager);
                    return;
                }
            }
        }
        ImageManagerSet newSet = new ImageManagerSet(imageManager.imageReference.imageSetName);
        newSet.add(imageManager);
        add(newSet);
        sortChildren();
    }
    
    @SuppressWarnings("unchecked")
    private void sortChildren(){
        children.sort((Object o1, Object o2) -> o1.toString().compareTo(o2.toString()));        
    }

    final public String updateImageSets()  {
        removeAllChildren();
        for (ImageManagerSet imageManagerSet : getImageManagerSets()) {
            add(imageManagerSet);
        }
        return MessageStrings.SUCCESS;
    }
    
    abstract public ArrayList<ImageManagerSet> getImageManagerSets();
    
    abstract public ImageManager getImageManager(ImageReference imageReference);
    
    public void removeImage(ImageReference imageReference) throws PermissionDenied {
        for (Object object:children) {
            if (object instanceof ImageManagerSet){
                ImageManagerSet imageSet = (ImageManagerSet) object;
                for (ImageManager im:imageSet.getImageManagerSet()){
                    if (im.imageReference.equals(imageReference)){
                        imageSet.remove(im);
                        if (imageSet.getImageManagerSet().isEmpty()){
                            children.remove(imageSet);
                        }
                        return;
                    }
                }
            }
        }
    }

    abstract public void clearROI_Folder(ImageReference imageReference, long folderID) throws PermissionDenied;

    abstract public void clearROI(ImageReference imageReference, long folderID, long roiID) throws PermissionDenied;

    public ArrayList<ImageManager> getAllImageManagers() {
        if (children.isEmpty()){
            updateImageSets();
        }
        ArrayList<ImageManager> imageManagers = new ArrayList<>();
        for (Object object: children) {
            if (object instanceof ImageManagerSet){
                ImageManagerSet imageManagerSet = (ImageManagerSet) object;
                for (ImageManager imageManager : imageManagerSet.getImageManagerSet()) {
                    imageManagers.add(imageManager);
                }
            }
        }
        return imageManagers;
    }

    abstract public String delete() throws PermissionDenied;

    abstract public String getUsers();

    abstract public Permissions getPermissions(String username) throws PermissionDenied;

    abstract public String setPermission(String username, Permissions permission) throws PermissionDenied;

    abstract public Permissions getPermissions();

    public void close() {
        removeAllStudyChangeListeners();
        for (TreeTableModelStudy model:annoSetModels){
            model.getAnnotationSet().removeListener(listenerAnnotationSet);
        }
        annoSetModels.clear();
    }
    
    abstract public StudySource cloneStudy(String cloneName);

    abstract public ImagesSource getImagesSource();

    protected final ListenerAnnotationSet listenerAnnotationSet = new ListenerAnnotationSet() {
        @Override
        public void add(AnnotationSet annoSet, ROI_Folder folder) {
            StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, -1, StudyChangeEvent.Type.Update, avmSession.username, folder.toXML(false));
            addChanges(event);
        }

        @Override
        public void remove(AnnotationSet annoSet, ROI_Folder folder) {
            StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, -1, StudyChangeEvent.Type.Delete, avmSession.username, "");
            addChanges(event);
        }

        @Override
        public void add(AnnotationSet annoSet, ROI_Folder folder, ROI roi) {
            StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, roi.id, StudyChangeEvent.Type.Update, avmSession.username, roi.toXML());
            addChanges(event);
        }

        @Override
        public void remove(AnnotationSet annoSet, ROI_Folder folder, ROI roi) {
            StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, roi.id, StudyChangeEvent.Type.Delete, avmSession.username, "");
            addChanges(event);
        }

        @Override
        public void updated(AnnotationSet annoSet, ROI_Folder folder) {
            StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, -1, StudyChangeEvent.Type.Update, avmSession.username, folder.toXML(false));
            addChanges(event);
        }

        @Override
        public void updated(AnnotationSet annoSet, ROI_Folder folder, final ROI originalROI, final ROI newROI) {
            StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, newROI.id, StudyChangeEvent.Type.Update, avmSession.username, newROI.toXML());
            addChanges(event);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for (ROI roi:folder.getROIs()){
                        if (roi.id == originalROI.id){
                            if (roi.getLastModified().before(newROI.getLastModified())){
                                StudyChangeEvent event = new StudyChangeEvent(annoSet.imageReference, studyID, folder.id, originalROI.id, StudyChangeEvent.Type.Update, avmSession.username, originalROI.toXML());
                                applyChanges(event);                                
                            }
                        }
                    }
                }
            }, 4);
        }
    };

    abstract public String getImageReferenceSetsXML();

    abstract public String getAnnotationSetXML(ImageReference imageReference);

    abstract public void addStudyChangeListener(ImageReference imageReference, StudyChangeListener listener);
    abstract public void removeStudyChangeListener(StudyChangeListener listener);
    abstract public void removeAllStudyChangeListeners();

    abstract public void addChanges(StudyChangeEvent event);

    public ArrayList<ImageReference> removeUnusedImages() {
        ArrayList<ImageReference> imagesRemoved = new ArrayList<>();
        ArrayList<ImageManagerSet> imageManagerSets = getImageManagerSets();
        for (ImageManagerSet imageManagerSet : imageManagerSets) {
            for (ImageManager imageManager : imageManagerSet.getImageManagerSet()) {
                ImageReference imageReference = imageManager.imageReference;
                AnnotationSet annoSet = getAnnotationSet(imageReference);
                boolean toRemove = true;
                for (ROI_Folder folder : annoSet.getROI_Folders()) {
                    if (!folder.getROIs().isEmpty()) {
                        toRemove = false;
                        break;
                    }
                }
                if (toRemove) {
                    imagesRemoved.add(imageReference);
                    try {
                        removeImage(imageReference);
                    } catch (PermissionDenied ex) {
                        Logger.getLogger(StudySource.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return imagesRemoved;
    }

    public TreeTableModelStudy getAnnotationSetModel(ImageReference imageReference) {
        for (TreeTableModelStudy model : annoSetModels) {
            if (ImageID.hashesAreEqual(model.getAnnotationSet().imageReference.hash, imageReference.hash)) {
                return model;
            }
        }
        String xml = getAnnotationSetXML(imageReference);
        if (xml == null) {
            TreeTableModelStudy model = new TreeTableModelStudy(new AnnotationSet(imageReference));
            annoSetModels.add(model);
            return model;
        }
        if (xml.contains(Permissions.PERMISSION_DENIED)) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, new PermissionDenied());
            return null;
        }
        final AnnotationSet annoSet;
        try {
            annoSet = AnnotationSet.parse(xml, imageReference);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        annoSet.addListener(listenerAnnotationSet);
        addStudyChangeListener(imageReference, changeListenerPrivate);
        TreeTableModelStudy model = new TreeTableModelStudy(annoSet);
        annoSetModels.add(model);
        return model;
    }

}

