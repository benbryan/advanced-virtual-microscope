package avl.sv.shared.study;

import avl.sv.shared.study.xml.StudyChangeEvent_XML_Writer;
import avl.sv.shared.study.xml.StudyChangeEvent_XML_Parser;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.KVStoreRef;
import avl.sv.shared.AVM_Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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

public class StudyChangeLoggerKVStoreManager {

    static void removeAllListeners(int studyID, AVM_Session session) {
        for (StudyChangeLoggerSession test : StudyChangeLoggerKVStoreManager.getInstance().loggers) {
            if (test.avmSession != session) {
                continue;
            }
            if (test.studyID != studyID) {
                continue;
            }
            removeLoggerElement(test);
        }
    }

    final List<StudyChangeLoggerSession> loggers;
    private static final int STUDY_CHANGE_LISTENER_POLL_PEROID_MS = 1000;
    final ExecutorService executors;

    private StudyChangeLoggerKVStoreManager() {
        loggers = Collections.synchronizedList(new ArrayList<StudyChangeLoggerSession>());
        executors = Executors.newFixedThreadPool(10);
    }
    private static StudyChangeLoggerKVStoreManager instance = null;

    public static StudyChangeLoggerKVStoreManager getInstance() {
        if (instance == null) {
            instance = new StudyChangeLoggerKVStoreManager();
        }
        return instance;
    }

    public static long addChangeLogger(int studyID, ImageReference imageReference, AVM_Session session, StudyChangeListener listener) {
        KVStore kvstore = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        ArrayList<String> minor = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            long loggerID = (long) ((Math.pow(2, 8) * (new Date()).getTime()) + (Math.random() * 255));

            major.clear();
            minor.clear();
            major.add("study");
            major.add("changeLoggers");
            kvstore.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
            minor.add(String.valueOf(loggerID));
            if (null != kvstore.get(Key.createKey(major, minor))) {
                // try again with a different loggerID, this one is taken
                // statistically this should not happen, but it could with 
                // a junky random number generator
                continue;
            }
            StudyChangeLogger logger = new StudyChangeLogger(session, loggerID, studyID, imageReference);
            kvstore.put(Key.createKey(major, minor), Value.createValue(logger.toString().getBytes()));

            major.clear();
            minor.clear();
            major.add("study");
            major.add(String.valueOf(studyID));
            minor.add("changeLoggerIDs");
            kvstore.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
            minor.add(imageReference.imageSetName);
            kvstore.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
            minor.add(imageReference.imageName);
            kvstore.putIfAbsent(Key.createKey(major, minor), Value.EMPTY_VALUE);
            minor.add(String.valueOf(loggerID));
            kvstore.put(Key.createKey(major, minor), Value.EMPTY_VALUE);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (StudyChangeEvent event:getAndClearEvents(loggerID, session.username)){
                        try {
                            listener.studyChanged(event);
                        } catch (Exception ex){
                            addChangeEvent(event);
                        }
                    }
                }
            }, STUDY_CHANGE_LISTENER_POLL_PEROID_MS, STUDY_CHANGE_LISTENER_POLL_PEROID_MS);
            StudyChangeLoggerSession loggerElement = new StudyChangeLoggerSession(session, studyID, imageReference, listener, timer, loggerID);
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

    public static void addChangeEvent(StudyChangeEvent event) {
        getInstance().executors.submit(() -> {
            ArrayList<String> sessionsAlreadyHandeled = new ArrayList<>();
            for (StudyChangeLoggerSession logger : StudyChangeLoggerKVStoreManager.getInstance().loggers) {
                if (logger.studyID != event.studyID) {
                    continue;
                }
                if (!logger.imageReference.equals(event.imageReference)) {
                    continue;
                }
                try {
                    logger.studyChangeListener.studyChanged(event);
                } catch (Exception ex){
                    Logger.getLogger(StudyChangeLoggerKVStoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                sessionsAlreadyHandeled.add(logger.avmSession.sessionID);
            }

            KVStore kvstore = KVStoreRef.getRef();
            ArrayList<String> major = new ArrayList<>();
            major.add("study");
            major.add(String.valueOf(event.studyID));
            ArrayList<String> minor = new ArrayList<>();
            minor.add("changeLoggerIDs");
            minor.add(event.imageReference.imageSetName);
            minor.add(event.imageReference.imageName);
            for (Key key : kvstore.multiGetKeys(Key.createKey(major, minor), null, Depth.CHILDREN_ONLY)) {
                List<String> temp = key.getMinorPath();
                String loggerID = temp.get(temp.size() - 1);
                major.clear();
                minor.clear();
                major.add("study");
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
                StudyChangeLogger logger = StudyChangeLogger.parse(changeLoggerString);
                if (logger == null) {
                    // logger is bad, may as well delete it
                    kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
                    kvstore.multiDelete(key, null, Depth.PARENT_AND_DESCENDANTS);
                    continue;
                }
                if (sessionsAlreadyHandeled.contains(logger.session.sessionID)) {
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
                String eventString = StudyChangeEvent_XML_Writer.toXML(event);
                kvstore.put(Key.createKey(major, minor), Value.createValue(eventString.getBytes()));
            }
        });
    }

    public static ArrayList<StudyChangeEvent> getAndClearEvents(long loggerID, String username) {
        ArrayList<StudyChangeEvent> events = new ArrayList<>();
        KVStore kv = KVStoreRef.getRef();
        ArrayList<String> major = new ArrayList<>();
        ArrayList<String> minor = new ArrayList<>();
        major.add("study");
        major.add("changeLoggers");
        minor.add(String.valueOf(loggerID));
        byte[] value = getValue(kv.get(Key.createKey(major, minor)));
        if (value == null) {
            return events;
        }
        StudyChangeLogger logger = null;
        try {
            logger = StudyChangeLogger.parse(new String(value));
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
                    events.add(StudyChangeEvent_XML_Parser.parse(eventString));
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                    Logger.getLogger(StudyChangeLoggerKVStoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return events;
    }

    private static void removeLoggerElement(StudyChangeLoggerSession loggerElement) {
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
        major.add("study");
        major.add("changeLoggers");
        minor.add(String.valueOf(loggerID));
        ValueVersion vv = kvstore.get(Key.createKey(major, minor));
        if (null == getValue(vv)) {
            // logger does not exist
            return;
        }
        kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);

        StudyChangeLogger logger = StudyChangeLogger.parse(new String(getValue(vv)));
        if (logger == null) {
            return;
        }

        major.clear();
        minor.clear();
        major.add("study");
        major.add(String.valueOf(logger.studyID));
        minor.add("changeLoggerIDs");
        minor.add(logger.imageReference.imageSetName);
        minor.add(logger.imageReference.imageName);
        minor.add(String.valueOf(loggerID));
        kvstore.multiDelete(Key.createKey(major, minor), null, Depth.PARENT_AND_DESCENDANTS);
        });
    }

    public static void remove(StudyChangeListener listener) {
        for (StudyChangeLoggerSession test : StudyChangeLoggerKVStoreManager.getInstance().loggers) {
            if (test.studyChangeListener.equals(listener)) {
                removeLoggerElement(test);
            }
        }
    }

}
