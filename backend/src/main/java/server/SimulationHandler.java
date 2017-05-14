package server;

import domain.Agent;
import domain.World;
import domain.util.Location;
import domain.vessel.SmartBoat;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Lea
 */
@WebSocket
public class SimulationHandler {

    private World world;

    private List<Agent> agents = Arrays.asList(new Agent[] {
            new SmartBoat(new Location(4.23434, -3.23345))
    });

    @OnWebSocketConnect
    public void onConnect(Session sess) throws Exception {
        System.out.println("Connection made");
        Server.getSessions().put(sess, new World(sess, agents));
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
