package avl.sv.shared.study;

import avl.sv.shared.study.xml.StudyChangeEvent_XML_Writer;
import avl.sv.shared.study.xml.StudyChangeEvent_XML_Parser;
import avl.sv.shared.image.ImageReference;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class StudyChangeEvent{
    
    public enum Type {
        Update  (1<<2),
        Delete  (1<<4),
        AddListener (1<<5), 
        RemoveListener (1<<6), 
        AddImage (1<<7),
        RemoveImage (1<<7);

        private final int value;
        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        } 
    }
    
    public final ImageReference imageReference;
    public final long folderID, roiID;
    public final int studyID;
    public final Type type;
    public final String username;
    public final String eventData;
    public boolean posted = false;

    public StudyChangeEvent(ImageReference imageReference, int studyID, long folderID, long roiID, Type type, String username, String eventData) {
        this.imageReference = imageReference; 
        this.studyID = studyID;
        this.folderID = folderID;
        this.roiID = roiID;
        this.type = type;
        this.username = username;
        this.eventData = eventData;
    }

    public static StudyChangeEvent parse(String xml) throws ParserConfigurationException, SAXException, IOException{
        return StudyChangeEvent_XML_Parser.parse(xml);
    }
    public static String toXML(StudyChangeEvent changeEvents){
        return StudyChangeEvent_XML_Writer.toXML(changeEvents);
    }
}

