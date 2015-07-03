package avl.sv.server.study;

import avl.sv.shared.image.ImageReference;
import avl.sv.shared.study.StudyChangeListener;
import avl.sv.shared.study.StudySource;
import avl.sv.shared.study.StudySourceKVStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;

public class StudyPortChangeMonitorSessionManager {

    private Map<Session,Element> sessions;
    
    private static StudyPortChangeMonitorSessionManager instance = null;
    private StudyPortChangeMonitorSessionManager(){
        sessions = Collections.synchronizedMap(new HashMap<Session,Element>());
    }
    public static StudyPortChangeMonitorSessionManager getInstance(){
        if (instance == null){
            instance = new StudyPortChangeMonitorSessionManager();
        }
        return instance;
    }
    
    public static class Element{
        final HashMap<ImageReference, StudyChangeListener> listeners;
        final StudySource studySource;

        public Element(StudySource studySource) {
            this.studySource = studySource;
            listeners = new HashMap<>();
        }
        
    }
    
    public static void openSession(Session session, StudySourceKVStore studySource) {
        StudyPortChangeMonitorSessionManager.getInstance().sessions.put(session, new Element(studySource));
    }
    public static void closeSession(Session session) {
        Element element = StudyPortChangeMonitorSessionManager.getInstance().sessions.get(session);
        if (element == null){
            return;
        }
        for (StudyChangeListener listener:element.listeners.values()){
            element.studySource.removeStudyChangeListener(listener);
        }
    }
    public static void addListener(Session session, ImageReference imageReference, StudyChangeListener listener) {
        Element element = StudyPortChangeMonitorSessionManager.getInstance().sessions.get(session);
        if (element == null){
            return;
        }
        element.listeners.put(imageReference, listener);
        element.studySource.addStudyChangeListener(imageReference, listener);
    }
    public static void removeListener(Session session, ImageReference imageReference) {
        Element element = StudyPortChangeMonitorSessionManager.getInstance().sessions.get(session);
        if (element == null){
            return;
        }
        StudyChangeListener listener = element.listeners.remove(imageReference);
        if (listener != null){
            element.studySource.removeStudyChangeListener(listener);
        }
    }
}

