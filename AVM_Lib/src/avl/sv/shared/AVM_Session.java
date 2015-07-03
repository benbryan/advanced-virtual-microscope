package avl.sv.shared;

public class AVM_Session {
    public final String username;
    public final String sessionID;

    public AVM_Session(String username, String sessionID) {
        this.username = username;
        this.sessionID = sessionID;
    }

    @Override
    public String toString() {
        return username + ", " + sessionID;
    }
    
}
