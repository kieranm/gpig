package server;

import domain.World;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;

/**
 * @author Oliver Lea
 */
public class Server {

    private static Map<Session, World> sessions = new ConcurrentHashMap<>();

    public Server(int port) {
        staticFiles.location("public");
        staticFiles.expireTime(600);
        webSocket("/sim", SimulationHandler.class);
    }

    public void run() {
        init();
    }

    public static Map<Session, World> getSessions() {
        return sessions;
    }
}
