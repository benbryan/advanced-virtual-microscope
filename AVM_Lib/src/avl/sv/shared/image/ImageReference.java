package avl.sv.shared.image;

import avl.sv.shared.image.ImageID;
import avl.sv.shared.image.ImageManager;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;

public class ImageReference implements  Comparable {
    public final String imageName;
    public final String imageSetName;
    public final byte[] hash;
    public final String hashString;

    public ImageReference(String imageSetName, String imageName, byte[] hash) {
        this.imageSetName = imageSetName;
        this.imageName = imageName;
        this.hash = hash;
        this.hashString = DatatypeConverter.printBase64Binary(hash);
    }
    public ImageReference(ImageReference imageReference) {
        this.imageSetName = imageReference.imageSetName;
        this.imageName = imageReference.imageName;
        this.hash = imageReference.hash;
        this.hashString = DatatypeConverter.printBase64Binary(imageReference.hash);
    }
    
    public ImageReference(String xml) {
        if (!xml.startsWith("<ImageReference")){
            throw new IllegalArgumentException("Image reference xml string is not formatted correctly");
        }
        xml = xml.substring(xml.indexOf(" ")+1);
        ArrayList<Integer> marks = new ArrayList<>();
        int state = 0;
        char[] xmlCharArray = xml.toCharArray();
        for (int i = 0; i < xmlCharArray.length; i++){
            char c = xmlCharArray[i];
            switch (state){
                case 0:
                    if (c == '='){
                        state = 1;
                    }                    
                    break;
                case 1:
                    if (c == '\"'){
                        state = 2;
                    }  
                    break;
                case 2:
                    if (c == '\"'){
                        state = 0;
                        marks.add(i+2);
                    }  
                    break;
            }
        }
        ArrayList<String> parts = new ArrayList<>();
        for (int i = 0; i < marks.size(); i++){
            if (i==0){
                parts.add(xml.substring(0, marks.get(0)));
            } else if (i == marks.size()){
                parts.add(xml.substring(marks.get(marks.size()-1), xml.length()-1));
            } else {
                parts.add(xml.substring(marks.get(i-1), marks.get(i)));
            }
        }
               
        if (parts.size() != 3){
            throw new IllegalArgumentException("Image reference xml string is not formatted correctly");
        }
        HashMap<String,String> entrys = new HashMap<>();      
        for (String part:parts){
            int i = part.indexOf('=');
            String name = part.substring(0, i);
            String value = part.substring(i+2, part.length()-2);
            entrys.put(name, value);
        }
        imageName = entrys.get("ImageName");
        imageSetName = entrys.get("ImageSetName");
        hashString = entrys.get("Hash");
        hash = DatatypeConverter.parseBase64Binary(hashString);
    }
    
    public String toXML(){
        return new StringBuilder().append("<ImageReference ImageName=\"").append(imageName).append("\" ImageSetName=\"").append(imageSetName).append("\" Hash=\"").append(getHashString()).append("\"/>").toString();
    }
    
    public String getHashString(){
        return DatatypeConverter.printBase64Binary(hash);
    }
    
    @Override
    public String toString() {
        return imageName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImageReference){
            ImageReference ref = (ImageReference) obj;
            if (!imageName.equals(ref.imageName)){
                return false;
            }
            if (!imageSetName.equals(ref.imageSetName)){
                return false;
            }
            if (!ImageID.hashesAreEqual(hash, ref.hash)){
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if ((hash == null) || hash.length < 4){
            return -1;
        }
        int out = 0;
        for (int i = 0; i < 4; i++){
            out += hash[i]*(2^(i*8));
        }
        return out;
    }

    @Override
    public int compareTo(Object o) {
        return o.toString().compareTo(toString());
    }
    
    
}
