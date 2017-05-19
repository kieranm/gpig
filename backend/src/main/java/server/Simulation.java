package server;

import domain.Agent;
import domain.World;
import domain.port.Port;
import domain.util.Coordinates;
import domain.vessel.FreightShip;
import domain.vessel.Ship;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class Simulation {

    private static final long PERIOD = 200L;
    private static final long INITIAL_DELAY = 1000L;

    // Mapbox Credentials
    private static final String MAPBOX_DATASET_ID_LEGACY = "cj2thnez5003q2qrzdczjgxil";

    private Timer timer;
    private World world;
    private Session session;

    public Simulation(Session session) {
        this.session = session;

        try {
            // TODO generate both worlds here (different data set ID)
            world = generateWorld(MAPBOX_DATASET_ID_LEGACY);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Simulation.this.tick();
                Simulation.this.sendToRemote();
            }
        };
        this.timer = new Timer();
        this.timer.schedule(tt, INITIAL_DELAY, PERIOD);
    }

    private World generateWorld(String mapboxDatasetId) throws IOException {

        // Create a WorldLoader
        WorldLoader loader = new WorldLoader(mapboxDatasetId);

        // Pull the port agents from the WorldLoader
        Map<String, Port> ports = loader.getPorts();
        System.out.println(ports);

        // Create agents
        List<Ship> ships = new ArrayList<>();

        // TODO how do we generate the ships
        Coordinates c = new Coordinates(
                ports.get("Liverpool").getCoordinates().getLatitude(),
                ports.get("Liverpool").getCoordinates().getLongitude()
        );
        Ship s = new FreightShip(c, 5, 5);
        ports.get("Liverpool").addShip(s);
        ports.get("Liverpool").generateRoute(s);
        ships.add(s);

        // Merge port and ship agents
        List<Agent> agents = new ArrayList<>(ships);
        agents.addAll(ports.values());

        World w = new World(agents);
        return w;

    }

    public void end() {
        this.timer.cancel();
    }

    private void tick() {
        world.tick();
    }


    /**
     * Method to embed message object in an object with details considering the message context (messageType).
     * Example types include 'update', 'settings', etc.
     */
    private JSONObject formatMessage(JSONObject jsonBody, String messageType) {
        JSONObject obj = new JSONObject()
                .put("message_type", messageType)
                .put("message_body", jsonBody);

        return obj;
    }

    private void sendToRemote() {
        try {
            session.getRemote().sendString(String.valueOf(formatMessage(world.toJSON(), "update")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
