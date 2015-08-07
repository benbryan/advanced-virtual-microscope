package avl.sv.shared.solution;

import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Writer;
import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Parser;
import avl.sv.shared.KVStoreRef;
import avl.sv.shared.AVM_Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import org.xml.sax.SAXException;

public class SolutionChangeLoggerKVStoreManager {

    static void removeAllListeners(int solutionID, AVM_Session session) {
        for (SolutionChangeLoggerSession test : SolutionChangeLoggerKVStoreManager.getInstance().loggers) {
            if (test.avmSession != session) {
                continue;
            }
            if (test.solutionID != solutionID) {
                continue;
            }
            removeLoggerElement(test);
        }
    }

    final List<SolutionChangeLoggerSession> loggers;
    private static final int STUDY_CHANGE_LISTENER_POLL_PEROID_MS = 1000;
    final ExecutorService executors;

    private SolutionChangeLoggerKVStoreManager() {
        loggers = Collections.synchronizedList(new ArrayList<SolutionChangeLoggerSession>());
        executors = Executors.newFixedThreadPool(10);
    }
    private static SolutionChangeLoggerKVStoreManager instance = null;

    public static SolutionChangeLoggerKVStoreManager getInstance() {
        if (instance == null) {
            instance = new SolutionChangeLoggerKVStoreManager();
        }
        return instance;
    }

    public static long addChangeLogger(int solutionID, AVM_Session session, SolutionChangeListener listener) {
        KVStore kvstore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        ArrayList<String> minor = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            long loggerID = (long) ((Math.pow(2, 8) * (new Date()).getTime()) + (Math.random() * 255));

            major.clear();
            minor.clear();
            major.add("solution");
            major.add("changeLoggers");
            kvstore.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
            minor.add(String.valueOf(loggerID));
            if (null != kvstore.get(Key.createKey(major, minor))) {
                // try again with a different loggerID, this one is taken
                // statistically this should not happen, but it could with 
                // a junky random number generator
                continue;
            }
            SolutionChangeLogger logger = new SolutionChangeLogger(session, loggerID, solutionID);
            kvstore.put(Key.createKey(major, minor), Value.createValue(logger.toString().getBytes()));

            major.clear();
            minor.clear();
            major.add("solution");
            major.add(String.valueOf(solutionID));
            minor.add("changeLoggerIDs");
            kvstore.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
            minor.add(String.valueOf(loggerID));
            kvstore.put(Key.createKey(major, minor), Value.EMPTY_VALUE);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (SolutionChangeEvent event:getAndClearEvents(loggerID, session.username)){
                        try {
                            listener.solutionChanged(event);
                        }catch (Exception ex){
                            Logger.getLogger(SolutionChangeLoggerKVStoreManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, STUDY_CHANGE_LISTENER_POLL_PEROID_MS, STUDY_CHANGE_LISTENER_POLL_PEROID_MS);
            SolutionChangeLoggerSession loggerElement = new SolutionChangeLoggerSession(session, solutionID, listener, timer, loggerID);
            getInstance().loggers.add(loggerElement);
            return loggerID;
        }
        return -1;
    }

    private static byte[] getValue(ValueVersion vv) {
        if ((vv == null) || (vv.getValue() == null) || (vv.getValue().getValue() == null)) {
            return null;
        } else {
            return vv.getValue().getValue();
        }
    }

    public static void addChangeEvent(SolutionChangeEvent event, String sessionID) {
        getInstance().executors.submit(() -> {
            ArrayList<String> sessionsAlreadyHandeled = new ArrayList<>();
            for (SolutionChangeLoggerSession logger : SolutionChangeLoggerKVStoreManager.getInstance().loggers) {
//                if (logger.avmSession.sessionID.equals(sessionID)) {
//                    // Dont need to log events from same session
//                    continue;
//                }
                if (logger.solutionID != event.solutionID) {
                    continue;
                }
                try {
                    logger.changeListener.solutionChanged(event);
                } catch (Exception ex){
                    Logger.getLogger(SolutionChangeLoggerKVStoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                sessionsAlreadyHandeled.add(sessionID);
            }

            KVStore kvstore = KVStoreRef.getRef();
            ArrayList<String> major = new ArrayList<>();
            major.add("solution");
            major.add(String.valueOf(event.solutionID));
            ArrayList<String> minor = new ArrayList<>();
            minor.add("changeLoggerIDs");
            for (Key key : kvstore.multiGetKeys(Key.createKey(major, minor), null, Depth.CHILDREN_ONLY)) {
                List<String> temp = key.getMinorPath();
                String loggerID = temp.get(temp.size() - 1);
                major.clear();
                minor.clear();
                major.add("solution");
                major.add("changeLoggers");
                minor.add(loggerID);

                // See if it still exists
                ValueVersion vv = kvstore.get(Key.createKey(major, minor));
                if (getValue(vv) == null) {
                    // Delete this pointer if it does not 
                    kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
                    continue;
                }

                String changeLoggerString = new String(vv.getValue().getValue());
                SolutionChangeLogger logger = SolutionChangeLogger.parse(changeLoggerString);
                if (logger == null) {
                    // logger is bad, may as well delete it
                    kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
                    kvstore.multiDelete(key, null, Depth.PARENT_AND_DESCENDANTS);
                    continue;
                }

//                if (logger.session.sessionID.equals(sessionID)) {
//                    // Dont need to log events from tbe same user in the same session
//                    continue;
//                }
                if (sessionsAlreadyHandeled.contains(sessionID)) {
                    continue;
                }

                minor.add("events");
                byte bIdx[] = getValue(kvstore.get(Key.createKey(major, minor)));
                String indexString;
                if (bIdx != null) {
                    long index = -1;
                    try {
                        index = Long.valueOf(new String(bIdx));
                    } catch (Exception ex) {
                    }
                    index++;
                    indexString = String.valueOf(index);
                } else {
                    indexString = String.valueOf(0);
                }

                kvstore.put(Key.createKey(major, minor), Value.createValue(indexString.getBytes()));
                minor.add(indexString);
                String eventString = SolutionChangeEvent_XML_Writer.toXML(event);
                kvstore.put(Key.createKey(major, minor), Value.createValue(eventString.getBytes()));
            }
        });
    }

    public static ArrayList<SolutionChangeEvent> getAndClearEvents(long loggerID, String username) {
        ArrayList<SolutionChangeEvent> events = new ArrayList<>();
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        ArrayList<String> minor = new ArrayList<>();
        major.add("solution");
        major.add("changeLoggers");
        minor.add(String.valueOf(loggerID));
        byte[] value = getValue(kv.get(Key.createKey(major, minor)));
        if (value == null) {
            return events;
        }
        SolutionChangeLogger logger = null;
        try {
            logger = SolutionChangeLogger.parse(new String(value));
        } catch (Exception ex) {
        }
        if (logger == null) {
            // logger is bad, may as well delete it
            kv.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
            return events;
        }
        if (!logger.session.username.equals(username)) {
            return events;
        }
        minor.add("events");
        
        Iterator<KeyValueVersion> gots = kv.multiGetIterator(Direction.FORWARD, 0, Key.createKey(major, minor), null, Depth.CHILDREN_ONLY);
        while (gots.hasNext()) {
            KeyValueVersion got = gots.next();
            if ((got.getValue() != null) || (got.getValue().getValue() != null)) {
                try {
                    String eventString = new String(got.getValue().getValue());
                    kv.delete(got.getKey());
                    events.add(SolutionChangeEvent_XML_Parser.parse(eventString));
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                    Logger.getLogger(SolutionChangeLoggerKVStoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return events;
    }

    private static void removeLoggerElement(SolutionChangeLoggerSession loggerElement) {
        getInstance().executors.submit(() -> {
            if (loggerElement == null) {
            return;
        }
        loggerElement.timer.cancel();
        long loggerID = loggerElement.loggerID;
        KVStore kvstore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        ArrayList<String> minor = new ArrayList<>();
        major.clear();
        minor.clear();
        major.add("solution");
        major.add("changeLoggers");
        minor.add(String.valueOf(loggerID));
        ValueVersion vv = kvstore.get(Key.createKey(major, minor));
        if (null == getValue(vv)) {
            // logger does not exist
            return;
        }
        kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);

        SolutionChangeLogger logger = SolutionChangeLogger.parse(new String(getValue(vv)));
        if (logger == null) {
            return;
        }

        major.clear();
        minor.clear();
        major.add("solution");
        major.add(String.valueOf(logger.solutionId));
        minor.add("changeLoggerIDs");
        minor.add(String.valueOf(loggerID));
        kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
        });
    }

    public static void remove(SolutionChangeListener listener) {
        for (SolutionChangeLoggerSession test : SolutionChangeLoggerKVStoreManager.getInstance().loggers) {
            if (test.changeListener.equals(listener)) {
                removeLoggerElement(test);
            }
        }
    }

}
