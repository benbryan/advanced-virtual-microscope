package avl.sv.client.solution;

import avl.sv.shared.AVM_ProgressMonitor;
import avl.sv.shared.solution.SolutionChangeEvent;
import avl.sv.shared.solution.xml.SolutionChangeEvent_XML_Parser;
import avl.sv.shared.solution.SolutionChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

@ClientEndpoint(decoders = {}, encoders = {})
public class SolutionChangeMonitorClient {

    SolutionChangeListener listener;
    Timer pingTimer;
    private static final long pingTimerPeroid = 60*1000;

    public SolutionChangeMonitorClient(SolutionChangeListener listener) {
        this.listener = listener;
    }
    
    @OnMessage
    public void onMessage(Session session, InputStream inputStream){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            int count = 0;
            char b[] = new char[1024];
            StringBuilder sb = new StringBuilder();
            while((count=br.read(b))>0){
                sb.append(b,0,count);
            }
            SolutionChangeEvent event = SolutionChangeEvent_XML_Parser.parse(sb.toString());
            listener.solutionChanged(event);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(SolutionChangeMonitorClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        Logger.getLogger(SolutionChangeMonitorClient.class.getName()).log(Level.WARNING, null, t);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        pingTimer.cancel();
        pingTimer = null;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        if (pingTimer != null){
            pingTimer.cancel();
        }
        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ByteBuffer bb = ByteBuffer.allocate(8);
                    bb.putLong(new Date().getTime());
                    session.getBasicRemote().sendPong((ByteBuffer) bb.flip());
                } catch (IOException | IllegalArgumentException ex) {
                    Logger.getLogger(SolutionChangeMonitorClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, pingTimerPeroid, pingTimerPeroid);
    }
    
    @OnMessage
    public void onPong(PongMessage pongMessage, Session session){
        LongBuffer lb = pongMessage.getApplicationData().asLongBuffer();
        if (lb.remaining()<1){
            return;
        }
        long time = lb.get();
    }
    
}
