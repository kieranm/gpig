package server;

import domain.Agent;
import domain.World;
import domain.port.LandPort;
import domain.util.Coordinates;
import domain.vessel.FreightShip;
import domain.vessel.Ship;
import domain.world.Edge;
import domain.world.Node;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Simulation {

    private final long PERIOD = 200l;
    private final long INITIAL_DELAY = 1000l;
    public final String EDGE_MAP_SEPERATOR = "CONNECTION";

    private Timer timer;
    private World world;
    private Session session;

    private void initialiseWorld(Path worldFilePath) throws IOException {
        List<Agent> agents = new ArrayList<>();
        Map<Integer, Node> nodes = new HashMap<>();
        Map<String, Edge> edges = new HashMap<>();

        String jsonReader = new String(Files.readAllBytes(worldFilePath));
        JSONObject object = new JSONObject(jsonReader);

        for (Object node : object.getJSONArray("nodes")) {
            JSONObject jNode = ((JSONObject) node);
            Integer id = jNode.getInt("id");
            double latitude = jNode.getDouble("latitude");
            double longitude = jNode.getDouble("longitude");

            nodes.put(id, new Node(id, new Coordinates(latitude,longitude)));
        }
        for (Object edge : object.getJSONArray("edges")) {
            JSONObject jEdge = ((JSONObject) edge);
            Integer start = jEdge.getInt("start");
            Integer end = jEdge.getInt("end");

            Edge newEdge = new Edge(nodes.get(start), nodes.get(end));
            edges.put(Integer.toString(start)+EDGE_MAP_SEPERATOR+Integer.toString(end), newEdge);
            edges.put(Integer.toString(end)+EDGE_MAP_SEPERATOR+Integer.toString(start), newEdge);
            nodes.get(start).addNeighbor(nodes.get(end));
            nodes.get(end).addNeighbor(nodes.get(start));
        }

        for (Object port : object.getJSONArray("ports")) {
            JSONObject jPort = ((JSONObject) port);
            String name = jPort.getString("name");
            int nodeID = jPort.getInt("node_id");
            int capacity = jPort.getInt("capacity");
            int load = jPort.getInt("load");

            //TODO double check whether default values are needed or attributes are guaranteed to be in JSON object
            int portSize = jPort.has("portSize") ? jPort.getInt("portSize") : 100 ;
            int cargoMoveSpeed = jPort.has("cargoMoveSpeed") ? jPort.getInt("cargoMoveSpeed") : 100;


            // TODO port variants
            LandPort p = new LandPort(name, nodes.get(nodeID), capacity, load);
            agents.add(p);
        }

        // TODO have the agents defined in a json file?
        Coordinates c = new Coordinates(nodes.get(0).getCoordinates().getLatitude(), nodes.get(0).getCoordinates().getLongitude());
        Ship s = new FreightShip(c, 5, 5);
        agents.add(s);

        this.world = new World(agents);
    }

    public Simulation(Session session) {
        this.session = session;

        try {
            initialiseWorld(Paths.get("./src/main/resources/ShippingNetwork.json"));
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

    public void end() {
        this.timer.cancel();
    }

    private void tick() {
        world.tick();
    }

    public void setMultiplier(int multiplier) {
        this.world.setMultiplier(multiplier);
    }

    public int getMultiplier() {
        return this.world.getMultiplier();
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
