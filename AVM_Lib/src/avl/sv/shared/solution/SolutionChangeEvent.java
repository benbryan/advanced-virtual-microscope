package avl.sv.shared.solution;

import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Parser;
import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Writer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class SolutionChangeEvent {
    
    public enum Type {
        ClassNames  (1<<1),   
        Description  (1<<2),
        ModelSetup  (1<<3),
        Classifier  (1<<4),
        Full        (1<<5);

        private final int value;
        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        } 
    }
    public final int solutionID;
    public final Type type;
    public final String username;
    public final String eventData;

    public SolutionChangeEvent(int solutionID, Type type, String username, String eventData) {
        this.solutionID = solutionID;
        this.type = type;
        this.username = username;
        this.eventData = eventData;
    }   
    
    public SolutionChangeEvent parse(String xml){
        try {
            return SolutionChangeEvent_XML_Parser.parse(xml);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SolutionChangeEvent.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    public String toXML(){
        return SolutionChangeEvent_XML_Writer.toXML(this);
    }
    @Override
    public String toString() {
        return toXML();
    }
    
    
}
