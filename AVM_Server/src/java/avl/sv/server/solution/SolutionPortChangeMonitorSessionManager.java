package avl.sv.server.solution;

import avl.sv.shared.solution.SolutionChangeListener;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.solution.SolutionSourceKVStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;

public class SolutionPortChangeMonitorSessionManager {

    private Map<Session,Element> sessions;
    
    private static SolutionPortChangeMonitorSessionManager instance = null;
    private SolutionPortChangeMonitorSessionManager(){
        sessions = Collections.synchronizedMap(new HashMap<Session,Element>());
    }
    public static SolutionPortChangeMonitorSessionManager getInstance(){
        if (instance == null){
            instance = new SolutionPortChangeMonitorSessionManager();
        }
        return instance;
    }
    
    public static class Element{
        final ArrayList<SolutionChangeListener> listeners;
        final SolutionSource solutionSource;

        public Element(SolutionSource solutionSource) {
            this.solutionSource = solutionSource;
            listeners = new ArrayList<>();
        }
    }
    
    public static void openSession(Session session, SolutionSourceKVStore solutionSource) {
        SolutionPortChangeMonitorSessionManager.getInstance().sessions.put(session, new Element(solutionSource));
    }
    public static void closeSession(Session session) {
        Element element = SolutionPortChangeMonitorSessionManager.getInstance().sessions.get(session);
        if (element == null){
            return;
        }
        for (SolutionChangeListener listener:element.listeners){
            element.solutionSource.removeSolutionChangeListener(listener);
        }
    }
    public static void addListener(Session session, SolutionChangeListener listener) {
        Element element = SolutionPortChangeMonitorSessionManager.getInstance().sessions.get(session);
        if (element == null){
            return;
        }
        element.listeners.add(listener);
    }
    public static void removeListener(Session session, SolutionChangeListener listener) {
        Element element = SolutionPortChangeMonitorSessionManager.getInstance().sessions.get(session);
        if (element == null){
            return;
        }
        element.listeners.remove(listener);
    }
}

