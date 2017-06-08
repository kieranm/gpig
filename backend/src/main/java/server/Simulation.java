package server;

import domain.Agent;
import domain.World;
import domain.port.Port;
import domain.vessel.Ship;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class Simulation {

    private final long PERIOD = 150;
    private final long INITIAL_DELAY = 01;

    private final int NUMBER_OF_SHIPS = 1000;

    private final String LEGACY_WORLD_KEY = "legacy";
    private final String OCEAN_WORLD_KEY = "oceanx";

    // Mapbox Credentials
    private static final String MAPBOX_DATASET_ID_LEGACY = "cj34dnbw300242qp7m97oh4n5";
    private static final String MAPBOX_DATASET_ID_OCEANX = "cj34wj9xi002r33o9abhk90iq";

    private Timer timer;
    private World world;
    private Session session;

    private World legacyWorld;
    private World oceanXWorld;

    public Simulation(Session session) {
        this.session = session;

        try {
            legacyWorld = generateWorld(MAPBOX_DATASET_ID_LEGACY);
            oceanXWorld = generateWorld(MAPBOX_DATASET_ID_OCEANX);
            world = legacyWorld;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Simulation.this.tick();
                Simulation.this.sendToRemote();
                Simulation.this.world.getStats().nextTick();
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
        List<Ship> ships = loader.generateShips(NUMBER_OF_SHIPS);

        // Merge port and ship agents
        List<Agent> agents = new ArrayList<>(ships);
        agents.addAll(ports.values());

        if(mapboxDatasetId.equals(MAPBOX_DATASET_ID_OCEANX))
            agents.addAll(loader.generateWeather(ports));

        return new World(agents);
    }

    public void end() {
        this.timer.cancel();
    }

    private void tick() {
        world.tick();
    }

    public void setMultiplier(int multiplier) {
        this.legacyWorld.setMultiplier(multiplier);
        this.oceanXWorld.setMultiplier(multiplier);
    }

    public void switchWorld(String worldKey) {
        switch (worldKey) {
            case LEGACY_WORLD_KEY:
                world = legacyWorld;
                break;
            case OCEAN_WORLD_KEY:
                world = oceanXWorld;
                break;
        }
    }

    public int getMultiplier() {
        return this.world.getMultiplier();
    }

    public void setShowWeather(boolean bla){
        this.world.setShowWeather(bla);
    }


    /**
     * Method to embed message object in an object with details considering the message context (messageType).
     * Example types include 'update', 'settings', etc.
     */
    private JSONObject formatMessage(JSONObject jsonBody, String messageType) {
        return new JSONObject()
                .put("message_type", messageType)
                .put("message_body", jsonBody);
    }

    private void sendToRemote() {
        try {
            session.getRemote().sendString(String.valueOf(formatMessage(world.toJSON(), "update")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
