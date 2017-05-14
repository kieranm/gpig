package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * @author Oliver Lea
 */
@WebSocket
public class SimulationHandler {

    @OnWebSocketConnect
    public void onConnect(Session sess) throws Exception {
        System.out.println("Connection established");
        Server.getSessions().put(sess, new Simulation(sess));
    }

    @OnWebSocketClose
    public void onClose(Session sess, int statusCode, String reason) throws Exception {
        Server.getSessions().remove(sess);
    }

    @OnWebSocketMessage
    public void onMessage(Session sess, String message) throws Exception {
        // Not likely to receive messages from the front-end
        System.out.println(message);
    }
}
