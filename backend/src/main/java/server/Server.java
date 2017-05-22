package server;

import static spark.Spark.*;

/**
 * @author Oliver Lea
 */
public class Server {


    public Server(int port) {
        staticFiles.location("public");
        staticFiles.expireTime(600);
        webSocket("/sim", SimulationHandler.class);
    }

    public void run() {
        init();
    }
}
