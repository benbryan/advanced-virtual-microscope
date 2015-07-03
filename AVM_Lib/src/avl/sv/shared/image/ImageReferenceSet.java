package avl.sv.shared.image;

import java.io.Serializable;
import java.util.ArrayList;

public class ImageReferenceSet implements Serializable   {
    private final String name;   
    
    private ArrayList<ImageReference> imageReferences = new ArrayList<>();
    
    public ImageReferenceSet(String name) {
        this.name = name;
    }
    
    public static ArrayList<ImageReferenceSet> parse(String xml){
        return ImageReferenceSetXML_Parser.parse(xml);
    }
    
    public static String toXML(ArrayList<ImageReferenceSet> imageReferenceSets){
        return ImageReferenceSetXML_Writer.getXMLString(imageReferenceSets);
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public ArrayList<ImageReference> getImageReferenceSet() {
        return imageReferences;
    }

    public void add(ImageReference imageReferenceNew) {
        for (ImageReference imageReference : imageReferences) {
            if (imageReference.equals(imageReferenceNew)) {
                return;
            }
        }
        imageReferences.add(imageReferenceNew);
        sortChildren();
    }
    
    @SuppressWarnings("unchecked")
    private void sortChildren(){
        imageReferences.sort((Object o1, Object o2) -> o1.toString().compareTo(o2.toString()));        
    }
}
