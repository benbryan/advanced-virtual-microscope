/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.client;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.*;

@ClientEndpoint
public class WSClient {

    private static Object waitLock = new Object();

    @OnMessage
    public void onMessage(String message) {
        //the new USD rate arrives from the websocket server side.
        System.out.println("Received msg: " + message);
    }

    private static void wait4TerminateSignal() {
        synchronized (waitLock) {
            try {
                waitLock.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) {
        WebSocketContainer container;
        Session session = null;
        try {
//Tyrus is plugged via ServiceLoader API. See notes above
            container = ContainerProvider.getWebSocketContainer();

//WS1 is the context-root of my web.app
//ratesrv is the path given in the ServerEndPoint annotation on server implementation
            session = container.connectToServer(WSClient.class, URI.create("ws://localhost:8080/WS1/ratesrv"));
            wait4TerminateSignal();
        } catch (Exception ex) {
            Logger.getLogger(WSClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException ex) {
                    Logger.getLogger(WSClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
