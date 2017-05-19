package server;

import domain.Agent;
import domain.World;
import domain.port.CoastalPort;
import domain.port.OffshorePort;
import domain.port.Port;
import domain.util.Coordinates;

import domain.vessel.FreightShip;
import domain.vessel.Ship;
import domain.world.Node;
import domain.world.Route;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.IdGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

        // Create a GeoJsonSpec
        GeoJsonSpec spec = new GeoJsonSpec(mapboxDatasetId);

        // Variables used to represent agents
        ArrayList<String> portNames = new ArrayList<>();
        List<Port> portAgents = new ArrayList<>();
        List<Agent> agents = new ArrayList<>();

        // Variables used to represent features
        List<JSONObject> coastalPorts = spec.getCoastalPorts();
        List<JSONObject> offshorePorts = spec.getOffshorePorts();
        List<JSONObject> lanes = spec.getLanes();

        System.out.println(coastalPorts);
        System.out.println(offshorePorts);
        System.out.println(lanes);

        // TODO put in a function
        // Add the Coastal Ports to the simulation
        for(Integer i=0; i < coastalPorts.size(); i++) {

            // Add node
            Node node = new Node(IdGenerator.getId(), new Coordinates(
                    (double)coastalPorts.get(i).getJSONObject("geometry").getJSONArray("coordinates").get(1),
                    (double)coastalPorts.get(i).getJSONObject("geometry").getJSONArray("coordinates").get(0)
            ));

            // Add agent
            CoastalPort p = new CoastalPort(
                    coastalPorts.get(i).getJSONObject("properties").getString("name"),
                    node,
                    coastalPorts.get(i).getJSONObject("properties").getInt("capacity"),
                    0);

            portAgents.add(p);
            node.setPort(p);

            portNames.add(coastalPorts.get(i).getJSONObject("properties").getString("name"));

        }

        // Add the Offshore Ports to the simulation. The IDs pick up where the Coastal ports left off.
        for(Integer i=0; i < offshorePorts.size(); i++) {

            Node node = new Node(IdGenerator.getId(), new Coordinates(
                    (double)offshorePorts.get(i).getJSONObject("geometry").getJSONArray("coordinates").get(1),
                    (double)offshorePorts.get(i).getJSONObject("geometry").getJSONArray("coordinates").get(0)
            ));

            // Add agent
            OffshorePort p = new OffshorePort(
                    offshorePorts.get(i).getJSONObject("properties").getString("name"),
                    node,
                    offshorePorts.get(i).getJSONObject("properties").getInt("capacity"),
                    0);
            node.setPort(p);
            portAgents.add(p);

            portNames.add(offshorePorts.get(i).getJSONObject("properties").getString("name"));
        }



        Map<String, Port> portMap = new HashMap<>();
        for (int i = 0; i < portNames.size(); i++) {
            portMap.put(portNames.get(i), portAgents.get(i));
        }

        // create routes
        //legacy
        for (JSONObject jLegacyLane : lanes) {
            List<Node> nodes = new ArrayList<>();
            Port start = portMap.get(jLegacyLane.getJSONObject("properties").getString("start"));
            Port end = portMap.get(jLegacyLane.getJSONObject("properties").getString("end"));

            nodes.add(start.getNode());
            for (Object jNode : jLegacyLane.getJSONObject("geometry").getJSONArray("coordinates")) {
                JSONArray jCoordinates = ((JSONArray) jNode);
                Node node = new Node(IdGenerator.getId(),
                        new Coordinates(jCoordinates.getDouble(1), jCoordinates.getDouble(0)));
                nodes.add(node);
            }
            nodes.add(end.getNode());

            int weight = jLegacyLane.getJSONObject("properties").getInt("weight");
            Route route = new Route(nodes, weight);

            start.addRoute(end, route);
            end.addRoute(start, route.reverse());

        }

        // generate ships TODO how do we generate the ships
        //legacy
        //oceanx
        Coordinates c = new Coordinates(portAgents.get(0).getCoordinates().getLatitude(), portAgents.get(0).getCoordinates().getLongitude());
        Ship s = new FreightShip(c, 5, 5);
        s.startRoute(portAgents.get(0).generateRoute().getNodes());
        agents.add(s);

        // initalise worlds
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
