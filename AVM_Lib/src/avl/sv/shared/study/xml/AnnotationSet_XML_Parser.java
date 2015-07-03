package avl.sv.shared.study.xml;

import avl.sv.shared.image.ImageReference;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.image.ImageID;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class AnnotationSet_XML_Parser {

    public static AnnotationSet parse(File f) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(f);
        AnnotationSet oldSet = parse(doc, new ImageReference(f.getName(), f.getParentFile().getName(), ImageID.get(f)));
        return oldSet;
    }

    public static AnnotationSet parse(String s, ImageReference imageReference) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(s.getBytes());
        Document doc = docBuilder.parse(is);
        return parse(doc, imageReference);
    }

    private static AnnotationSet parse(Document doc, ImageReference imageReference) {
        doc.getDocumentElement().normalize();
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Annotations".equalsIgnoreCase(n.getNodeName())) {
                return parse(n, imageReference);
            }
        }
        return null;
    }

    public static AnnotationSet parse(Node n, ImageReference imageReference) {
        AnnotationSet annotationSet = new AnnotationSet(imageReference);
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("ROI_Folder".equalsIgnoreCase(n.getNodeName()) || "Annotation".equalsIgnoreCase(n.getNodeName())) {
                annotationSet.add(ROI_Folder_XML_Parser.parse(n), true);
            }
        }
        return annotationSet;
    }
}
















