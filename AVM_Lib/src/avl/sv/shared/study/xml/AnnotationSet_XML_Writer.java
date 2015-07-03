package avl.sv.shared.study.xml;

import avl.sv.shared.image.ImageReferenceSetXML_Writer;
import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.AnnotationSet;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnnotationSet_XML_Writer {
    public static String getXMLString(AnnotationSet annoSet) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();

            Element annotationsNode = doc.createElement("Annotations");
            doc.appendChild(annotationsNode);

            appendAnnotations(doc, annoSet, annotationsNode);

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bos.toString();
    }
    
    public static void appendAnnotations(Document doc, AnnotationSet annoSet, Element annotationsNode) {
        ImageReferenceSetXML_Writer.appendImageNode(doc, annotationsNode, annoSet.imageReference);
        for (ROI_Folder anno : annoSet.getROI_Folders()) {
            Element annotationNode = ROI_Folder_XML_Writer.createFolderElement(doc, anno, true);
            annotationsNode.appendChild(annotationNode);
        }
    }
        
}
