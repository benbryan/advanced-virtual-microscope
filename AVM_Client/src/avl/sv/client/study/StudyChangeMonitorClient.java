package avl.sv.client.study;

import avl.sv.shared.study.StudyChangeEvent;
import avl.sv.shared.study.xml.StudyChangeEvent_XML_Parser;
import avl.sv.shared.study.xml.StudyChangeEvent_XML_Writer;
import avl.sv.shared.study.StudyChangeListener;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;

@ClientEndpoint(decoders = StudyChangeEvent_XML_Parser.class, encoders = StudyChangeEvent_XML_Writer.class)
public class StudyChangeMonitorClient {

    private final StudyChangeListener listener;
    private final ConnectionListener connectionListener;

    public StudyChangeMonitorClient(StudyChangeListener listener, ConnectionListener connectionListener) {
        this.listener = listener;
        this.connectionListener = connectionListener;
    }

    @OnMessage
    public void onMessage(StudyChangeEvent event) {
        listener.studyChanged(event);
    }
    
    @OnError
    public void onError(Session session, Throwable t){
        connectionListener.onError(session, t);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        connectionListener.onClose(session, closeReason);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        connectionListener.onOpen(session, config);
    }
    
    @OnMessage
    public void onPong(PongMessage pongMessage, Session session){
        connectionListener.onPong( pongMessage, session);
    }
    
}
