package avl.sv.shared;


import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import oracle.kv.Depth;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;

public class SessionManagerKVStore extends SessionManager  {
    
    @Override
    public void purgeDayOldSessions() {
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("sessions");
        ArrayList<String> minor = new ArrayList<>();
        SortedMap<Key, ValueVersion> sessions = kv.multiGet(Key.createKey(major), null, Depth.CHILDREN_ONLY);
        for (Map.Entry<Key, ValueVersion> session : sessions.entrySet()) {
            minor.clear();
            minor.addAll(session.getKey().getMajorPath());
            minor.add("LoginTime");
            ValueVersion vv = kv.get(Key.createKey(session.getKey().getMajorPath(), minor));
            if ((vv == null) || (vv.getValue() == null) || vv.getValue().getValue() == null) {
                kv.multiDelete(session.getKey(), null, Depth.PARENT_AND_DESCENDANTS);
            } else {
                long timeSizeLogin = new Date().getTime() - Long.parseLong(new String(vv.getValue().getValue()));
                long oneDay = 1000*60*60*24;
                if (timeSizeLogin > oneDay) {
                    kv.multiDelete(session.getKey(), null, Depth.PARENT_AND_DESCENDANTS);
                }
            }
        }
    }

    @Override
    protected String getUsername(String sessionID) {
        ArrayList<String> major = new ArrayList<>();
        major.add("sessions");
        ArrayList<String> minor = new ArrayList<>();
        minor.add(sessionID);
        ValueVersion vv = KVStoreRef.getRef().get(Key.createKey(major, minor));
        if ((vv == null) || (vv.getValue() == null) || vv.getValue().getValue() == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        String username = new String(vv.getValue().getValue()).toLowerCase();
        return username;
    }

    @Override
    protected String addSession(String username) {
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        major.add("sessions");
        ArrayList<String> minor = new ArrayList<>();
        minor.add(createSessionID());
        int counter = 0;
        while (true) {
            Version v = kv.putIfAbsent(Key.createKey(major, minor), Value.createValue(username.toLowerCase().getBytes()));
            if (v == null) {
                minor.set(0, createSessionID());
            } else {
                break;
            }
            if (counter++ > 100) {
                return "error: this should not have happened";
            }
        }
        String sessionID = minor.get(0);
        minor.add("LoginTime");
        kv.put(Key.createKey(major, minor), Value.createValue(String.valueOf(new Date().getTime()).getBytes()));
        return sessionID;
    }

    
}
