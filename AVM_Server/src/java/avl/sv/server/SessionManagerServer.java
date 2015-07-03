package avl.sv.server;

import avl.sv.shared.SessionManagerMemory;
import com.sun.xml.ws.transport.Headers;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import javax.websocket.Session;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

public class SessionManagerServer extends SessionManagerMemory{
    
    protected SessionManagerServer() {  }

    private static SessionManagerServer instance = null;
   
    public static SessionManagerServer getInstance(){
        if (instance == null){
            instance = new SessionManagerServer();
        }
        return instance;
    }
     
    
    public static String getSessionID(WebServiceContext wsContext)  {
        final Object httpRequestObject = wsContext.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS);
        if (httpRequestObject instanceof Headers) {
            Headers httpRequest = (com.sun.xml.ws.transport.Headers) httpRequestObject;
            List<String> avmSessionIDs = httpRequest.get("avm_session_id");
            for (String id : avmSessionIDs) {
                return id;
            }
        }
        return null;
    }
    
}
