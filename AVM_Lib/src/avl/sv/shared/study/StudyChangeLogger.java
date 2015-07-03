package avl.sv.shared.study;

import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageReference;

public class StudyChangeLogger {
    public final AVM_Session session;
    public long loggerID;
    public final long studyID;
    public final ImageReference imageReference;
    
    public StudyChangeLogger(AVM_Session session, long lastDate, long studyID, ImageReference imageReference) {
        this.session = session;
        this.loggerID = lastDate;
        this.studyID = studyID;
        this.imageReference = imageReference; 
    }

    public static StudyChangeLogger parse(String loggerAsString) {
        String temp[] = loggerAsString.split(",");
        if (temp.length != 5) {
            return null;
        }
        
        String username = temp[0];
        String sessionID = temp[1];
        AVM_Session session = new AVM_Session(username, sessionID);
        long loggerID = Long.valueOf(temp[2]);
        long studyID = Integer.parseInt(temp[3]);
        ImageReference imageReference = new ImageReference(temp[4]);
        return new StudyChangeLogger(session, loggerID, studyID, imageReference);
    }

    @Override
    public String toString() {
        return session.username + "," + session.sessionID + "," + String.valueOf(loggerID) + "," + studyID + "," + imageReference.toXML();
    }

}
