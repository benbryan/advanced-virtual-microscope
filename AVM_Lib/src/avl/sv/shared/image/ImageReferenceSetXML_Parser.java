package avl.sv.shared.image;

import avl.sv.shared.NamedNodeMapFunc;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageReferenceSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.MutableTreeNode;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ImageReferenceSetXML_Parser {
                     
    public static ArrayList<ImageReferenceSet> parse(String s) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance( );
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(s.getBytes());
            Document doc = docBuilder.parse(is);             
            return parse(doc);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(ImageReferenceSetXML_Parser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private static ArrayList<ImageReferenceSet> parse(Document doc) {
        doc.getDocumentElement().normalize();
        ArrayList<ImageReferenceSet> imageSets = new ArrayList<>();
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("ImageSets".equalsIgnoreCase(n.getNodeName())) {
                for (ImageReferenceSet imageSet:parseImageSets(n)){
                    imageSets.add(imageSet);
                }
            }
        }
        return imageSets;
    }
    
    private static ArrayList<ImageReferenceSet> parseImageSets(Node n) {
        ArrayList<ImageReferenceSet> imageSets = new ArrayList<>();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("ImageSet".equalsIgnoreCase(n.getNodeName())) {
                NamedNodeMap a = n.getAttributes();
                String imageSetName = NamedNodeMapFunc.getString(a,"name");
                ArrayList<ImageReference> imageReferences = new ArrayList<>();
                for (ImageReference imageReference:parseImages(n)){
                    imageReferences.add(imageReference);
                }
                Collections.sort(imageReferences, new Comparator<ImageReference>() {
                    @Override
                    public int compare(ImageReference o1, ImageReference o2) {
                        return o1.imageName.compareTo(o2.imageName);
                    }
                });
                ImageReferenceSet imageSet = new ImageReferenceSet(imageSetName);
                for (ImageReference imageReference:imageReferences){
                    imageSet.add(imageReference);
                }
                imageSets.add(imageSet);
            }
        }
        return imageSets;
    }

    public static ArrayList<ImageReference> parseImages(Node n) {
        ArrayList<ImageReference> imageReferences = new ArrayList<>();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("ImageReference".equalsIgnoreCase(n.getNodeName())) {
                NamedNodeMap a = n.getAttributes();
                String imageName = NamedNodeMapFunc.getString(a,"ImageName");
                String imageSetName = NamedNodeMapFunc.getString(a,"ImageSetName");
                String hashBase64 = NamedNodeMapFunc.getString(a,"Hash");
                byte hash[] = DatatypeConverter.parseBase64Binary(hashBase64);
                imageReferences.add(new ImageReference(imageSetName, imageName, hash));
            }
        }
        return imageReferences;
    }
           
}
