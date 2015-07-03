package avl.sv.shared.solution;

import avl.sv.shared.study.*;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageReference;

public class SolutionChangeLogger {
    public final AVM_Session session;
    public long loggerID;
    public final long solutionId;
    
    public SolutionChangeLogger(AVM_Session session, long lastDate, long studyID) {
        this.session = session;
        this.loggerID = lastDate;
        this.solutionId = studyID;
    }

    public static SolutionChangeLogger parse(String loggerAsString) {
        String temp[] = loggerAsString.split(",");
        if (temp.length != 5) {
            return null;
        }
        
        String username = temp[0];
        String sessionID = temp[1];
        AVM_Session session = new AVM_Session(username, sessionID);
        long loggerID = Long.valueOf(temp[2]);
        long studyID = Integer.parseInt(temp[3]);
        return new SolutionChangeLogger(session, loggerID, studyID);
    }

    @Override
    public String toString() {
        return session.username + "," + session.sessionID + "," + String.valueOf(loggerID) + "," + solutionId;
    }

}
