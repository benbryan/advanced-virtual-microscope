package avl.sv.shared.solution.xml;

import avl.sv.shared.solution.xml.*;
import avl.sv.shared.image.ImageReferenceSetXML_Parser;
import avl.sv.shared.NamedNodeMapFunc;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.solution.SolutionChangeEvent;
import avl.sv.shared.solution.SolutionChangeEvent.Type;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SolutionChangeEvent_XML_Parser implements Decoder.Text<SolutionChangeEvent> {
        
    public static SolutionChangeEvent parse(String s) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance( );
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(s.getBytes());
        Document doc = docBuilder.parse(is);
        return SolutionChangeEvent_XML_Parser.parseChangeEvent(doc);             
    }
        
    public static SolutionChangeEvent parseChangeEvent(Document n1) throws ParserConfigurationException, SAXException, IOException {
        for (Node n = n1.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("ChangeEvent".equalsIgnoreCase(n.getNodeName())) {
                NamedNodeMap a = n.getAttributes();
                int solutionId = NamedNodeMapFunc.getInteger(a,"Solution");
                Type type = Type.valueOf(NamedNodeMapFunc.getString(a, "Type"));
                String username = NamedNodeMapFunc.getString(a, "Username");
                String eventData = NamedNodeMapFunc.getString(a, "EventData");
                return new SolutionChangeEvent(solutionId,  type, username, eventData);
            }
        }
        return null;
    }  

    @Override
    public SolutionChangeEvent decode(String arg0) throws DecodeException {
        try {
            return parse(arg0);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SolutionChangeEvent_XML_Parser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean willDecode(String arg0) {
        return arg0 != null;
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
    
    
     
}
