package avl.sv.shared;

import avl.sv.shared.AVM_Session;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionManagerMemory extends SessionManager {
        
    private static HashMap<String, AVM_Session> sessions = new HashMap<>();
    private static HashMap<AVM_Session, Date> loginTimes = new HashMap<>();
    
    @Override
    public void purgeDayOldSessions() { 
        Date mark = new Date(System.currentTimeMillis()- new Date().getTime());
        ArrayList<AVM_Session> oldSessions = new ArrayList<>();
        for (Map.Entry<AVM_Session, Date> entry:loginTimes.entrySet()){
            Date date = entry.getValue();
            if (date.before(mark)){
                oldSessions.add(entry.getKey());
            }
        }
        for (AVM_Session session:oldSessions){
            sessions.remove(session.sessionID);
            loginTimes.remove(session);
        }    
    }

    @Override
    protected String getUsername(String sessionID) {
        AVM_Session session = sessions.get(sessionID);
        if (session == null){
            return null;
        } else {
            return session.username;
        }
    }
    
    @Override
    protected String addSession(String username) {
        String sessionID = createSessionID();
        AVM_Session session = new AVM_Session(username, sessionID);
        sessions.put(sessionID, session);
        loginTimes.put(session, new Date());
        return session.sessionID;
    }

}
