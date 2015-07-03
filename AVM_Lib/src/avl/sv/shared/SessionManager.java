package avl.sv.shared;

abstract public class SessionManager {
    
    abstract public void purgeDayOldSessions();
    abstract protected String addSession(String username);
    abstract protected String getUsername(String sessionID);
            
    public AVM_Session getSession(String sessionID){
        String username = getUsername(sessionID);
        if ((sessionID==null) || (username==null)){
            return null;
        }
        return new AVM_Session(username, sessionID);
    }
    
    public String login(String username, String password) {
        final String INVALID = "error: Invalid username or password";
        if (User_KVStore.isValid(username, password)) {
            return addSession(username.toLowerCase());
        } else {
            return INVALID;
        }
    }    
    
    protected String createSessionID() {
        return Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE))
                + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE))
                + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE))
                + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE));
    }
    
}
