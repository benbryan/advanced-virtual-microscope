package avl.sv.shared.image;

import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageReferenceSet;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ImageReferenceSetXML_Writer {
    
    public static String getXMLString(ArrayList<ImageReferenceSet> imageSets) {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
      
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            appendImageSets(doc, imageSets);
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            
            StreamResult result = new StreamResult(bos);
            transformer.transform(source, result);

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return bos.toString();
    }

    protected static void appendImageSets(Document doc, ArrayList<ImageReferenceSet> imageSets) {
        Element imageSetsNode = doc.createElement("ImageSets");
        doc.appendChild(imageSetsNode);
        for ( ImageReferenceSet imageSet : imageSets) {
            Element imageSetNode = doc.createElement("ImageSet");
            imageSetNode.setAttribute("name", imageSet.getName());
            imageSetsNode.appendChild(imageSetNode);
            appendImageSet(doc, imageSetNode, imageSet);
        }
    }
    
    protected static void appendImageSet(Document doc, Element imageSetNode, ImageReferenceSet imageSet) {       
        for (ImageReference imageRef:imageSet.getImageReferenceSet()){
            appendImageNode(doc, imageSetNode, imageRef);
        }
    }
    
    public static void appendImageNode(Document doc, Element imageSetNode, ImageReference imageRef) {       
        Element imageNode = doc.createElement("ImageReference");
        imageNode.setAttribute("ImageName", imageRef.imageName);
        imageNode.setAttribute("ImageSetName", imageRef.imageSetName);
        imageNode.setAttribute("Hash", imageRef.getHashString());
        imageSetNode.appendChild(imageNode);
    }

}
