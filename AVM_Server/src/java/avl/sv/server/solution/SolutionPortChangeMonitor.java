package avl.sv.server.solution;

import avl.sv.server.SessionManagerServer;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.solution.SolutionChangeEvent;
import avl.sv.shared.solution.SolutionChangeListener;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.solution.SolutionSourceKVStore;
import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

@ServerEndpoint(value = "/SolutionPort/ChangeMonitor/{sessionID}/{solutionID}", encoders = {}, decoders = {})
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
        if (solutionSource == null) {
            return;
        }
        if (!solutionSource.getPermissions().canRead()) {
            return;
        }
        SolutionChangeListener listener = new SolutionChangeListener() {
            @Override
            public void solutionChanged(SolutionChangeEvent event) {
                if (!session.isOpen()) {
                    SolutionPortChangeMonitorSessionManager.removeListener(session, this);
                    return;
                }
                try {
                    byte b[] = event.toXML().getBytes();
                    OutputStream os = session.getBasicRemote().getSendStream();
                    int idx = 0;
                    final int s = 1024;
                    while(idx<b.length){
                       os.write(b, idx, Math.min(s, b.length-idx)); 
                       idx+=s;
                    }
                    os.close();
                } catch (IOException ex) {
                    Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        if (isRemove) {
            solutionSource.removeSolutionChangeListener(listener);
            SolutionPortChangeMonitorSessionManager.removeListener(session, listener);
        } else {
            solutionSource.addSolutionChangeListener(listener);
            SolutionPortChangeMonitorSessionManager.addListener(session, listener);
        }
    }    
   
    @OnMessage
    public void onMessage( @PathParam("sessionID") String sessionID,
                            @PathParam("solutionID") Integer solutionID,
                            Session session, 
                            InputStream inputStream){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            int count = 0;
            char b[] = new char[1024];
            StringBuilder sb = new StringBuilder();
            while((count=br.read(b))>0){
                sb.append(b,0,count);
            }
            SolutionChangeEvent event = SolutionChangeEvent_XML_Parser.parse(sb.toString());
            avl.sv.shared.AVM_Session avmSession = getSession(sessionID);
            if (avmSession == null) {return;}
            SolutionSource solutionSource;
            try {
                solutionSource = SolutionSourceKVStore.get(avmSession, solutionID);
            } catch (PermissionDenied ex) {
                Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            if (solutionSource == null) {
                return;
            }
            if (!solutionSource.getPermissions().canRead()) {
                return;
            }
            if (event.type.equals(SolutionChangeEvent.Type.Full)){
                solutionSource.setSolution(event.eventData);
            }
        } catch (IOException | ParserConfigurationException | SAXException | PermissionDenied ex) {
            Logger.getLogger(SolutionPortChangeMonitor.class.getName()).log(Level.SEVERE, null, ex);
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
