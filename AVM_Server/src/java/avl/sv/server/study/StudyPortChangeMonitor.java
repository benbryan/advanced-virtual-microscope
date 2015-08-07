package avl.sv.server.study;

import avl.sv.server.SessionManagerServer;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.study.StudyChangeEvent;
import avl.sv.shared.study.xml.StudyChangeEvent_XML_Writer;
import avl.sv.shared.study.StudyChangeListener;
import avl.sv.shared.study.StudySource;
import avl.sv.shared.study.StudySourceKVStore;
import avl.sv.shared.study.xml.StudyChangeEvent_XML_Parser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/StudyPort/ChangeMonitor/{sessionID}/{studyID}", encoders = StudyChangeEvent_XML_Writer.class, decoders = StudyChangeEvent_XML_Parser.class)
public class StudyPortChangeMonitor {   
    
    private avl.sv.shared.AVM_Session getSession(String sessionID){
        return SessionManagerServer.getInstance().getSession(sessionID);
    }
   
    @OnMessage
    public void onMessage(  @PathParam("sessionID") String sessionID,
                            @PathParam("studyID") Integer studyID,
                            StudyChangeEvent event,
                            Session session ) {
        avl.sv.shared.AVM_Session avmSession = getSession(sessionID);
        event = new StudyChangeEvent(event.imageReference, event.studyID, event.folderID, event.roiID, event.type, avmSession.username, event.eventData);
        StudySourceKVStore studySource;
        try {
            studySource = StudySourceKVStore.get(avmSession, event.studyID);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        ImageReference imageReference = event.imageReference;
        if (studySource == null) {
            return;
        }
        switch (event.type) {
            case AddListener:
                final StudyChangeListener listener = (StudyChangeEvent eventNew) -> {
                    if (!session.isOpen()){
                        StudyPortChangeMonitorSessionManager.removeListener(session, imageReference);
                        return;
                    }
                    session.getAsyncRemote().sendObject(eventNew);
                };            
                StudyPortChangeMonitorSessionManager.addListener(session, imageReference, listener);
                break;
            case RemoveListener:
                StudyPortChangeMonitorSessionManager.removeListener(session, imageReference);
                break;
            default:
                studySource.addChanges(event);
                break;
        }
    }    
    
    @OnError
    public void error(Session session, Throwable t) {
        System.out.println("StudyPortChangeMonitor throwing");
        Logger.getLogger(StudyPortChangeMonitor.class.getName()).log(Level.WARNING, null, t);
        System.out.println("/StudyPortChangeMonitor throwing");
    }

    @OnOpen 
    public void onOpen( @PathParam("sessionID") String sessionID,
                        @PathParam("studyID") Integer studyID,
                        Session session){
        avl.sv.shared.AVM_Session avmSession = getSession(sessionID);
        StudySourceKVStore studySource;
        try {
            studySource = StudySourceKVStore.get(avmSession, studyID);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        StudyPortChangeMonitorSessionManager.openSession(session, studySource);
    }
    
    @OnClose
    public void onClose(Session session) {
        StudyPortChangeMonitorSessionManager.closeSession(session);
    }
    
    @OnMessage
    public void onPong(PongMessage pongMessage, Session session){
        try {
            LongBuffer lb = pongMessage.getApplicationData().asLongBuffer();
            if (lb.remaining()<1){
                return;
            }
            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putLong(new Date().getTime());
            session.getBasicRemote().sendPong((ByteBuffer) bb.flip());
        } catch (IOException | IllegalArgumentException ex) {
            Logger.getLogger(StudyPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
}
