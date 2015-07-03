package avl.sv.shared.study.xml;

import avl.sv.shared.NamedNodeMapFunc;
import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.Attribute;
import avl.sv.shared.study.ROI;
import java.awt.Polygon;
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

public class ROI_XML_Writer {
    
    public static String getXMLString(ROI roi) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element annotationNode = createRegion(doc, roi);
            doc.appendChild(annotationNode);

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bos.toString();
    }

    protected static Element createRegion(Document doc, ROI r) {
        String negativeROA = (r.negativeROA) ? "1" : "0";
        String analyze = (r.isAnalyze()) ? "1" : "0";
        String type = String.valueOf(r.getType());
        String fixedSize = String.valueOf(r.isFixedSize());
        String isSelected = String.valueOf(r.selected);
        String posted = String.valueOf(r.isPosted);
        Element regionNode = doc.createElement("Region");

        regionNode.setAttribute("Id", String.valueOf(r.id));
        regionNode.setAttribute("Type", type);
        regionNode.setAttribute("Text", r.getName());
        regionNode.setAttribute("NegativeROA", negativeROA);
        regionNode.setAttribute("Analyze", analyze);
        regionNode.setAttribute("FixedSize", fixedSize);
        regionNode.setAttribute("Posted", posted);
        regionNode.setAttribute("Selected", isSelected);
        
        appendRegionAttributes(doc, r, regionNode);
        appendVertices(doc, r.getPolygon(), regionNode);      
        return regionNode;
    }
    protected static void appendVertices(Document doc, Polygon p, Element regionNode) {
        int x[] = p.xpoints;
        int y[] = p.ypoints;
        Element verticesNode = doc.createElement("Vertices");
        regionNode.appendChild(verticesNode);
        for (int i = 0; i < p.npoints; i++) {
            Element vertexNode = doc.createElement("Vertex");
            verticesNode.appendChild(vertexNode);
            vertexNode.setAttribute("X", String.valueOf(x[i]));
            vertexNode.setAttribute("Y", String.valueOf(y[i]));
        }
    }  
    protected static void appendRegionAttributes(Document doc, ROI r, Element regionNode) {
        Element attributesNode = doc.createElement("Attributes");
        regionNode.appendChild(attributesNode);
        for (Attribute a : r.attributes) {
            Element attributeNode = doc.createElement("Attribute");
            attributesNode.appendChild(attributeNode);
            attributeNode.setAttribute("Name", a.name);
            attributeNode.setAttribute("Id", String.valueOf(a.id));
            attributeNode.setAttribute("Value", a.value);
        }
    }
}
