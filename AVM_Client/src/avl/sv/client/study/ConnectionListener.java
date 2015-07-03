package avl.sv.client.study;
// yes this seems silly, but I could not think of another way to get these out of the annotated classes

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.PongMessage;
import javax.websocket.Session;

public interface ConnectionListener {
    public void onPong(PongMessage pongMessage, Session session);

    public void onOpen(Session session, EndpointConfig config);

    public void onClose(Session session, CloseReason closeReason);

    public void onError(Session session, Throwable t);
}
