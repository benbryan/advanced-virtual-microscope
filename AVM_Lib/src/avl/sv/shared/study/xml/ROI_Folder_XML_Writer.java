package avl.sv.shared.study.xml;

import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.Attribute;
import avl.sv.shared.study.ROI;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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

public class ROI_Folder_XML_Writer {

    public static String getXMLString(ROI_Folder anno, boolean includeROIs) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element annotationNode = createFolderElement(doc, anno, includeROIs);
            doc.appendChild(annotationNode);

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bos.toString();
    }
    
    protected static Element createFolderElement(Document doc, ROI_Folder folder, boolean includeROIs) {
        Element annotationNode = doc.createElement("ROI_Folder");

        String visible = (folder.visible) ? "1" : "0";

        annotationNode.setAttribute("Id", String.valueOf(folder.id));
        annotationNode.setAttribute("Name", folder.getName());
        annotationNode.setAttribute("LineColor", String.valueOf(folder.getLineColor().getRGB()));
        annotationNode.setAttribute("Visible", visible);

        appendAnnotationAttributes(doc, folder, annotationNode);
        if (includeROIs){
            appendRegions(doc, folder.getROIs(), annotationNode);
        }
        return annotationNode;
    }
    protected static void appendAnnotationAttributes(Document doc, ROI_Folder anno, Element annotationNode) {
        Element regionsNode = doc.createElement("Attributes");
        annotationNode.appendChild(regionsNode);
        for (Attribute a : anno.attributes) {
            Element attributeNode = doc.createElement("Attribute");
            regionsNode.appendChild(attributeNode);
            attributeNode.setAttribute("Name", a.name);
            attributeNode.setAttribute("Id", String.valueOf(a.id));
            attributeNode.setAttribute("Value", a.value);
        }
    }
    
    protected static void appendRegions(Document doc, ArrayList<ROI> rois, Element annotationNode) {
        Element regionsNode = doc.createElement("Regions");
        annotationNode.appendChild(regionsNode);
        for (ROI r : rois) {
            Element regionNode = ROI_XML_Writer.createRegion(doc, r );
            regionsNode.appendChild(regionNode);
        }
    }
}
