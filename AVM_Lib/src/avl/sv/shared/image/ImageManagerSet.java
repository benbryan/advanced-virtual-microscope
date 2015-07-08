package avl.sv.shared.image;

import avl.sv.shared.AVM_Source;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.tree.MutableTreeNode;

public class ImageManagerSet extends AVM_Source implements Serializable   {
    
    private final String name;   
    
    public ImageManagerSet(String name) {
        this.name = name;
    }
    
    public static String toXML(ArrayList<ImageManagerSet> imageManagerSets){
        ArrayList<ImageReferenceSet> imageReferenceSets = convert(imageManagerSets);
        return ImageReferenceSetXML_Writer.getXMLString(imageReferenceSets);
    }
    
    private static ArrayList<ImageReferenceSet> convert(ArrayList<ImageManagerSet> imageManagerSets) {
        ArrayList<ImageReferenceSet> imageReferenceSets = new ArrayList<>();
        for (ImageManagerSet imageManagerSet: imageManagerSets){
            ImageReferenceSet imageReferenceSet = new ImageReferenceSet(imageManagerSet.name);
            for (ImageManager imageManager:imageManagerSet.getImageManagerSet()){
                imageReferenceSet.add(imageManager.imageReference);
            }
            imageReferenceSets.add(imageReferenceSet);
        }
        return imageReferenceSets;
    }
        
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public ArrayList<ImageManager> getImageManagerSet() {
        if (children == null){
            return new ArrayList<>();
        }
        ArrayList<ImageManager> imageManagers = new ArrayList<>();       
        for (Object r : children) {
            if (r instanceof ImageManager) {
                imageManagers.add((ImageManager) r);
            }
        }
        return imageManagers;
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (newChild instanceof ImageManager) {
            if (children == null) {
                super.add(newChild);
                return;
            }
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).equals(newChild)) {
                    remove(i);
                }
            }
            super.add(newChild);
            sortChildren();
        } else {
            throw new IllegalArgumentException("ImageManagerSet only accepts ImageManagers");
        }
    }

    @SuppressWarnings("unchecked")
    private void sortChildren(){
        children.sort((Object o1, Object o2) -> o1.toString().compareTo(o2.toString()));        
    }

    @Override
    public String getDescription() {
        return "Not supported yet.";
    }

    @Override
    public String setDescription(String description) {
        return "Not supported yet.";
    }
}
