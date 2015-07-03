package avl.sv.server;

import avl.sv.shared.KVStoreRef;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.User_KVStore;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.websocket.WebSocketContainer;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "LoginPort")
public class LoginPort {
    
    @Resource
    private WebServiceContext wsContext;   
    private static SessionManagerServer sessionManager = SessionManagerServer.getInstance();
    
    private AVM_Session getSession(){
        return sessionManager.getSession(sessionManager.getSessionID(wsContext));
    }

    @WebMethod(operationName = "login")
    public String login( @WebParam(name = "username") final String username,
                         @WebParam(name = "password") final String password ) {        
        if (KVStoreRef.getRef() == null){
            return "error: the server cannot connect to it's database";
        }
        return sessionManager.login( username.toLowerCase(), password);
    }

    @WebMethod(operationName = "keepAlive")
    public String keepAlive() {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        return MessageStrings.SUCCESS;
    }

    @WebMethod(operationName = "registerUser")
    public String registerUser( @WebParam(name = "username") final String username, 
                                @WebParam(name = "password") final String password) {
        return User_KVStore.registerUser(username, password);
    }

    @WebMethod(operationName = "changePassword")
    public String changePassword( @WebParam(name = "oldPassword") final String oldPassword, 
                                  @WebParam(name = "password") final String newPassword) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        return User_KVStore.changePassword(session.username, oldPassword, newPassword);
    }
    
    

}
