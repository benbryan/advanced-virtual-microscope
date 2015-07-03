package avl.sv.server.solution;

import avl.sv.server.SessionManagerServer;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.solution.SolutionChangeEvent;
import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Writer;
import avl.sv.shared.solution.SolutionChangeListener;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.solution.SolutionSourceKVStore;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/SolutionPort/ChangeMonitor/{sessionID}/{solutionID}", encoders = SolutionChangeEvent_XML_Writer.class, decoders = {})
public class SolutionPortChangeMonitor {   
    
    
    private avl.sv.shared.AVM_Session getSession(String sessionID){
        return SessionManagerServer.getInstance().getSession(sessionID);
    }
    
    @OnMessage
    public void onMessage(  @PathParam("sessionID") String sessionID,
                            @PathParam("solutionID") Integer solutionID,
                            String action,
                            Session session ) {

        boolean isRemove;
        if (action.startsWith("stop:")){
            isRemove = true;
        } else if (action.startsWith("start:")){
            isRemove = false;
        } else {
            return;
        }
        avl.sv.shared.AVM_Session avmSession = getSession(sessionID);
        if (avmSession == null) {return;}
        SolutionSource solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(avmSession, solutionID);
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (solutionSource == null){return;}
        if (!solutionSource.getPermissions(avmSession.username).canRead()){
            return;
        }
        SolutionChangeListener listener = (SolutionChangeEvent event) -> {
            session.getAsyncRemote().sendObject(event);
        };
        if (isRemove){
            solutionSource.removeSolutionChangeListener(listener);
            SolutionPortChangeMonitorSessionManager.removeListener(session, listener);
        } else {
            solutionSource.addSolutionChangeListener(listener);
            SolutionPortChangeMonitorSessionManager.addListener(session, listener);
        }

    }    
   
    @OnError
    public void error(Session session, Throwable t) {
        Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.WARNING, null, t);
    }

    @OnOpen 
    public void onOpen( @PathParam("sessionID") String sessionID,
                        @PathParam("solutionID") Integer solutionID,
                        Session session){
        avl.sv.shared.AVM_Session avmSession = getSession(sessionID);
        try {
            SolutionPortChangeMonitorSessionManager.openSession(session, SolutionSourceKVStore.get(avmSession, solutionID));
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        SolutionPortChangeMonitorSessionManager.closeSession(session);
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
            Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
}
