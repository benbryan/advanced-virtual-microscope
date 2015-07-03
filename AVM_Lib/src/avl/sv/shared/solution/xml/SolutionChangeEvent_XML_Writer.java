package avl.sv.shared.solution.xml;

import avl.sv.shared.solution.SolutionChangeEvent;
import java.io.ByteArrayOutputStream;
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

public class SolutionChangeEvent_XML_Writer implements Encoder.Text<SolutionChangeEvent> {
       
    public static String toXML(SolutionChangeEvent changeEvent) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();       
            
            Element eventNode = doc.createElement("ChangeEvent");
            eventNode.setAttribute("Solution", String.valueOf(changeEvent.solutionID));
            eventNode.setAttribute("Type", changeEvent.type.name());
            eventNode.setAttribute("Username", changeEvent.username);
            eventNode.setAttribute("EventData", changeEvent.eventData);
            doc.appendChild(eventNode);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bos.toString();
    }

    @Override
    public String encode(SolutionChangeEvent arg0) throws EncodeException {
        return toXML(arg0);
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
    
}
