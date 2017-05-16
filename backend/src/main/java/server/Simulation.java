package server;

import domain.Agent;
import domain.World;
import domain.port.LandPort;
import domain.util.Coordinates;
import domain.vessel.FreightShip;
import domain.vessel.Ship;
import domain.world.Edge;
import domain.world.Node;
import domain.world.ShippingNetwork;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Oliver Lea
 */
public class Simulation {

    private static final long PERIOD = 1000l;
    private static final long INITIAL_DELAY = 1000l;
    public static final String EDGE_MAP_SEPERATOR = "CONNECTION";

    private Timer timer;
    private World world;
    private Session session;

    private void initialiseWorld(Path worldFilePath) throws IOException {
        List<Agent> agents = new ArrayList<Agent>();
        Map<Integer, Node> nodes = new HashMap<Integer, Node>();
        Map<String, Edge> edges = new HashMap<String, Edge>();

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
        ShippingNetwork sn = new ShippingNetwork(nodes, edges);

        for (Object port : object.getJSONArray("ports")) {
            JSONObject jPort = ((JSONObject) port);
            String name = jPort.getString("name");
            int nodeID = jPort.getInt("node_id");
            int capacity = jPort.getInt("capacity");
            int load = jPort.getInt("load");

            // TODO port variants
            LandPort p = new LandPort(name, nodes.get(nodeID), capacity, load);
            agents.add(p);
        }

        // TODO have the agents defined in a json file?
        Coordinates c = new Coordinates(nodes.get(0).getCoordinates().getLatitude(), nodes.get(0).getCoordinates().getLongitude());
        Ship s = new FreightShip(c, 5, 5);
        try {
            sn.calculateRoute(s, nodes.get(0), nodes.get(1));
        } catch (ShippingNetwork.NoRouteFoundException e) {
            e.printStackTrace();
        }
        agents.add(s);

        this.world = new World(agents, sn);
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

    private void sendToRemote() {
        try {
            session.getRemote().sendString(String.valueOf(world.toJSON()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
