
package avl.sv.shared.study.xml;

import avl.sv.shared.image.ImageReferenceSetXML_Writer;
import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.StudyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StudyChangeEvent_XML_Writer implements Encoder.Text<StudyChangeEvent> {
       
    public static String toXML(StudyChangeEvent changeEvent) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();       
            
            Element eventNode = doc.createElement("ChangeEvent");
            eventNode.setAttribute("Study", String.valueOf(changeEvent.studyID));
            eventNode.setAttribute("Folder", String.valueOf(changeEvent.folderID));
            eventNode.setAttribute("ROI", String.valueOf(changeEvent.roiID));
            eventNode.setAttribute("Type", changeEvent.type.name());
            eventNode.setAttribute("Username", changeEvent.username);
            eventNode.setAttribute("EventData", changeEvent.eventData);
            ImageReferenceSetXML_Writer.appendImageNode(doc, eventNode, changeEvent.imageReference);
            doc.appendChild(eventNode);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bos.toString();
    }

    @Override
    public String encode(StudyChangeEvent arg0) throws EncodeException {
        return toXML(arg0);
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
    
}
