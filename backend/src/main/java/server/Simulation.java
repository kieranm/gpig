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
import org.json.JSONException;
import org.json.JSONObject;
import utils.IdGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
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
    public static final String MAPBOX_USERNAME = "kieranmch";
    public static final String MAPBOX_ACCESS_TOKEN =
            "pk.eyJ1Ijoia2llcmFubWNoIiwiYSI6ImNqMnRrNWoycjAwMXgyeHBkdDR5NTVoOWIifQ.CcpzELblchnu7wV_waTkCw";
    public static final String MAPBOX_DATASET_ID_LEGACY = "cj2thnez5003q2qrzdczjgxil";

    private Timer timer;
    private World world;
    private World legacyWorld;
    private World oceanXWorld;
    private Session session;

    private World worldFromMapbox(String mapboxDatasetId) throws IOException {
        // Fetch JSON data from Mapbox
        String mapboxAPIURL = String.format("https://api.mapbox.com/datasets/v1/%s/%s/features?access_token=%s",
                MAPBOX_USERNAME, mapboxDatasetId, MAPBOX_ACCESS_TOKEN);
        InputStream response = new URL(mapboxAPIURL).openStream();

        List<Agent> agents = new ArrayList<>();

        // Variables used to represent features
        JSONArray features;
        List<JSONObject> coastalPorts = new ArrayList<>();
        List<JSONObject> offshorePorts = new ArrayList<>();
        List<String> portNames = new ArrayList<>();
        List<JSONObject> lanes = new ArrayList<>();

        // Agents for the simulation
        List<Port> portAgents = new ArrayList<>();

        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            features = new JSONObject(responseBody).getJSONArray("features");
        }

        for (Object feature : features) {
            JSONObject jFeature = (JSONObject) feature;
            String featureType = jFeature.getJSONObject("geometry").getString("type");

            // Process all the ports to begin with
            if (Objects.equals(featureType, "Point")) {

                String portType;
                String portName;
                Integer capacity;

                // Make sure the port name is specified
                try {
                    portName = jFeature.getJSONObject("properties").getString("name");
                } catch (JSONException e) {
                    throw new JSONException("The 'name' property was missing for Port " +
                            jFeature.getString("id"));
                }

                if(Objects.equals(portName, "")) {
                    throw new JSONException("The 'name' property was empty for Port " +
                            jFeature.getString("id"));
                }

                // Check port name is not duplicated
                if(portNames.stream().anyMatch(str -> str.trim().equals(portName))) {
                    throw new JSONException("The port name " + portName + " is used for two or more ports.");
                }
                portNames.add(portName);

                // Make sure the port type is specified
                try {
                    portType = jFeature.getJSONObject("properties").getString("type");
                } catch (JSONException e) {
                    throw new JSONException("The 'type' property was missing for Port -> " +
                            portName);
                }

                // Make sure the container capacity is specified
                try {
                    capacity = jFeature.getJSONObject("properties").getInt("capacity");
                } catch (JSONException e) {
                   // throw new JSONException("The 'capacity' property was missing for Port -> " +
                    //        portName);
                    capacity = 0;
                }

                // Add the port to the correct list
                if(Objects.equals(portType, "coastal_port")) {
                    coastalPorts.add(jFeature);
                    System.out.println("Adding Coastal Port --" +
                            " Name: " + portName +
                            ", Container Capacity: " + capacity
                    );
                } else if(Objects.equals(portType, "offshore_port")) {
                    offshorePorts.add(jFeature);
                    System.out.println("Adding Offshore Port --" +
                            " Name: " + portName +
                            ", Container Capacity: " + capacity
                    );
                } else {
                    throw new JSONException("The 'type' property was invalid for Port -> " +
                            portName);
                }

            }

        }

        // Reset portNames so they can be put in the correct order
        portNames = new ArrayList<>();
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

        // Now process the shipping lanes
        for (Object feature : features) {
            JSONObject jFeature = (JSONObject) feature;
            String featureType = jFeature.getJSONObject("geometry").getString("type");

            Integer laneWeight;
            String start;
            String end;

            // Process all the ports to begin with
            if (Objects.equals(featureType, "LineString")) {

                // Make sure the weight is specified
                try {
                    laneWeight = jFeature.getJSONObject("properties").getInt("weight");
                } catch (JSONException e) {
                    throw new JSONException("The 'weight' property was missing for Lane -> " +
                            jFeature.getString("id"));
                }

                // Make sure the start is specified
                try {
                    start = jFeature.getJSONObject("properties").getString("start");
                } catch (JSONException e) {
                    throw new JSONException("The 'start' property was missing for Lane -> " +
                            jFeature.getString("id"));
                }

                // Make sure the start is a valid port
                if(portNames.stream().noneMatch(str -> str.trim().equals(start))) {
                    throw new JSONException("The port name " + start + " does not exist.");
                }

                // Make sure the end is specified
                try {
                    end = jFeature.getJSONObject("properties").getString("end");
                } catch (JSONException e) {
                    throw new JSONException("The 'end' property was missing for Lane -> " +
                            jFeature.getString("id"));
                }

                // Make sure the end is a valid port
                if(portNames.stream().noneMatch(str -> str.trim().equals(end))) {
                    throw new JSONException("The port name " + end + " does not exist.");
                }

                // Add the lane to the list
                lanes.add(jFeature);
                // the lane has been added to the list



            }

        }

        System.out.println(lanes);

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

    public Simulation(Session session) {
        this.session = session;

        try {
            // TODO generate both worlds here (different data set ID)
            world = worldFromMapbox(MAPBOX_DATASET_ID_LEGACY);
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
