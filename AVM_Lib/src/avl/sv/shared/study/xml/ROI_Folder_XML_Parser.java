package avl.sv.shared.study.xml;

import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.Attribute;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIPoly;
import avl.sv.shared.NamedNodeMapFunc;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ROI_Folder_XML_Parser {
            
    public static ROI_Folder parse(String s) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance( );
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(s.getBytes());
        Document doc = docBuilder.parse(is);
        return ROI_Folder_XML_Parser.parse(doc);             
    }
    public static ROI_Folder parse(Document doc) throws ParserConfigurationException, SAXException, IOException {
        doc.getDocumentElement().normalize();
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("ROI_Folder".equalsIgnoreCase(n.getNodeName())) {
                return ROI_Folder_XML_Parser.parse(n);
            }
        }
        return null;
    }
    public static ROI_Folder parse(Node n) {
        ROI_Folder annotation = ROI_Folder.createDefault();
        NamedNodeMap a = n.getAttributes();
        if (a != null){
            annotation.selected = NamedNodeMapFunc.getBoolean(a, "Selected");
            annotation.visible = NamedNodeMapFunc.getBoolean(a, "Visible");
            annotation.id = NamedNodeMapFunc.getLong(a, "Id");
            annotation.setLineColor(new Color(NamedNodeMapFunc.getInteger(a, "LineColor")), true);
            annotation.setName(NamedNodeMapFunc.getString(a, "Name"), true);
        }
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
            if ("Attributes".equalsIgnoreCase(c.getNodeName())) {
                annotation.attributes = parseAnnotationAttributes(c);               
            }
            if ("Regions".equalsIgnoreCase(c.getNodeName())) {
                ArrayList<ROI> rois = ROI_XML_Parser.parseRegions(c);
                for (ROI roi:rois){
                    annotation.add(roi, true);
                }
            }
        }
        return annotation;
    }

    private static ArrayList<Attribute> parseAnnotationAttributes(Node n) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Attribute".equalsIgnoreCase(n.getNodeName())) {
                Attribute attr = new Attribute();
                attr.id = (NamedNodeMapFunc.getInteger(n.getAttributes(), "Id"));
                attr.name = (NamedNodeMapFunc.getString(n.getAttributes(), "Name"));
                attr.value = (NamedNodeMapFunc.getString(n.getAttributes(),"Value"));
            }
        }
        return attributes;
    }
    
}
