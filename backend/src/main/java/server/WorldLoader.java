package server;

import domain.port.AidSite;
import domain.extra.Weather;
import domain.port.CoastalPort;
import domain.port.OffshorePort;
import domain.port.Port;
import domain.util.AgentType;
import domain.util.Coordinates;
import domain.vessel.Aircraft;
import domain.vessel.FreightShip;
import domain.vessel.Ship;
import domain.vessel.SmartShip;
import domain.world.Node;
import domain.world.Route;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.IdGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

class WorldLoader {

    // Mapbox Credentials
    private static final String MAPBOX_USERNAME = "kieranmch";
    private static final String MAPBOX_ACCESS_TOKEN =
            "pk.eyJ1Ijoia2llcmFubWNoIiwiYSI6ImNqMnRrNWoycjAwMXgyeHBkdDR5NTVoOWIifQ.CcpzELblchnu7wV_waTkCw";
    private String datasetID;

    private JSONArray features;

    private List<JSONObject> coastalPorts = new ArrayList<>();
    private List<JSONObject> offshorePorts = new ArrayList<>();
    private List<JSONObject> lanes = new ArrayList<>();
    private Map<String, Port> portAgents = new HashMap<>();

    Map<String, Port> getPorts() {
        return this.portAgents;
    }

    WorldLoader(String datasetID) throws IOException {

        // Dataset ID determines which Mapbox Dataset to pull GeoJSON from
        this.datasetID = datasetID;

        // Perform HTTP GET request on Mapbox API to pull in GeoJSON information
        try {
            this.fetchFeatures();
        } catch (IOException e) {
            throw new IOException("Failed to connect to the Mapbox API.");
        }

        // Fetch ports and lanes
        this.fetchPorts();
        this.fetchLanes();

        // Generate ports based on the JSON data
        this.generatePorts(this.coastalPorts);
        this.generatePorts(this.offshorePorts);

        // Add the lanes to the ports
        this.addLanesToPorts();
    }

    private void fetchFeatures() throws IOException {

        JSONArray features;

        // Fetch JSON data from Mapbox
        String mapboxAPIURL = String.format("https://api.mapbox.com/datasets/v1/%s/%s/features?access_token=%s",
                MAPBOX_USERNAME, datasetID, MAPBOX_ACCESS_TOKEN);
        InputStream response = new URL(mapboxAPIURL).openStream();

        // Parse the response body
        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            features = new JSONObject(responseBody).getJSONArray("features");
        }

        this.features = features;

    }

    private void fetchPorts() {
        JSONObject jFeature;
        String featureType;
        String portType;

        for (Object feature: features)  {
            jFeature = (JSONObject) feature;
            featureType = jFeature.getJSONObject("geometry").getString("type");

            if (Objects.equals(featureType, "Point")) {
                this.verifyPort(jFeature);

                portType = jFeature.getJSONObject("properties").getString("type");
                if(Objects.equals(portType, "coastal_port")) {
                    coastalPorts.add(jFeature);
                } else {
                    offshorePorts.add(jFeature);
                }
            }
        }

    }

    private void fetchLanes() {

        JSONObject jFeature;
        String featureType;

        for (Object feature : features) {
            jFeature = (JSONObject) feature;
            featureType = jFeature.getJSONObject("geometry").getString("type");

            if (Objects.equals(featureType, "LineString")) {
                // This feature is a LineString
                this.verifyLane(jFeature);
                lanes.add(jFeature);
            }
        }

    }

    private void verifyPort(JSONObject port) {
        verifyAttribute(port, "name");
        verifyAttribute(port, "type");
        verifyAttribute(port, "capacity");
        verifyAttribute(port, "dock_capacity");

        String portName = port.getJSONObject("properties").getString("name");

        // Check that the port name is not a duplicate of any existing coastal or offshore port
        if(coastalPorts.stream().anyMatch(
                jsonObject -> jsonObject.getJSONObject("properties").getString("name").trim().equals(portName)) ||
           offshorePorts.stream().anyMatch(
                jsonObject -> jsonObject.getJSONObject("properties").getString("name").trim().equals(portName))) {
            throw new JSONException("The port name " + portName + " is used for two or more ports.");
        }

        //TODO: verify type is valid

    }

    private void verifyLane(JSONObject lane) {
        verifyAttribute(lane, "start");
        verifyAttribute(lane, "end");
        verifyAttribute(lane, "weight");

        String startName = lane.getJSONObject("properties").getString("start");
        String endName = lane.getJSONObject("properties").getString("end");

        // Make sure the start and end ports exist
        if((coastalPorts.stream().noneMatch(
                jsonObject -> jsonObject.getJSONObject("properties").getString("name").trim().equals(startName)) &&
            offshorePorts.stream().noneMatch(
                jsonObject -> jsonObject.getJSONObject("properties").getString("name").trim().equals(startName)))||
           (coastalPorts.stream().noneMatch(
                jsonObject -> jsonObject.getJSONObject("properties").getString("name").trim().equals(endName)) &&
            offshorePorts.stream().noneMatch(
                jsonObject -> jsonObject.getJSONObject("properties").getString("name").trim().equals(endName)))) {
            throw new JSONException("Verify that both " + startName + " and " + endName + " ports exist.");
        }

        //TODO: verify type is valid
    }

    private void verifyAttribute(JSONObject jFeature, String name) {
        if(!jFeature.getJSONObject("properties").has(name)) {
            throw new JSONException("The '" + name + "' property was missing for object with ID  -> " +
                    jFeature.getString("id"));
        }
    }

    private Node createNode(double latitude, double longitude) {
        Node node = new Node(IdGenerator.getId(), new Coordinates(latitude, longitude));
        return node;
    }

    private void generatePorts(List<JSONObject> jPorts) {
        double latitude;
        double longitude;
        String name;
        Node portNode;
        Port port;

        for(JSONObject jPort: jPorts) {
            name = jPort.getJSONObject("properties").getString("name");
            latitude = (double) jPort.getJSONObject("geometry").getJSONArray("coordinates").get(1);
            longitude = (double) jPort.getJSONObject("geometry").getJSONArray("coordinates").get(0);
            portNode = this.createNode(latitude, longitude);
            // TODO check the type of port to create
            if (jPort.getJSONObject("properties").getString("type").equals("coastal_port")) {
                port = new CoastalPort(
                        name,
                        portNode,
                        //jPort.getJSONObject("properties").getInt("capacity"),
                        10000,
                        //jPort.getJSONObject("properties").getInt("dock_capacity"),
                        5000
                );
            } else if (jPort.getJSONObject("properties").getString("type").equals("aid_port")) {
                port = new AidSite(
                        name,
                        portNode,
                        //jPort.getJSONObject("properties").getInt("capacity"),
                        1000,
                        //jPort.getJSONObject("properties").getInt("dock_capacity"),
                        1000
                );
            } else {
                port = new OffshorePort(
                        name,
                        portNode,
                        10000,
                        5000
                        //jPort.getJSONObject("properties").getInt("capacity"),
                        //jPort.getJSONObject("properties").getInt("dock_capacity")
                );
            }


            portAgents.put(name, port);
            portNode.setPort(port);
        }
    }

    private void addLanesToPorts() {
        List<Node> nodes;
        String startName;
        String endName;
        Port startPort;
        Port endPort;
        Route route;
        int weight;

        for(JSONObject jLane : this.lanes) {
            nodes = new ArrayList<>();

            startName = jLane.getJSONObject("properties").getString("start");
            endName = jLane.getJSONObject("properties").getString("end");
            startPort = this.portAgents.get(startName);
            endPort = this.portAgents.get(endName);

            // Add the start node
            nodes.add(startPort.getNode());

            // Iterate through the list of points on the lane and add nodes
            for (Object jPoint : jLane.getJSONObject("geometry").getJSONArray("coordinates")) {
                nodes.add(
                        this.createNode(((JSONArray)jPoint).getDouble(1), ((JSONArray)jPoint).getDouble(0))
                );
            }

            // Add the end node
            nodes.add(endPort.getNode());

            // Extract weight associated with the route
            weight = jLane.getJSONObject("properties").getInt("weight");

            // TODO REMOVE
            weight = 1;

            // Create a route and assign it to ports
            route = new Route(nodes, weight);
            startPort.addRoute(endPort, route);
            endPort.addRoute(startPort, route.reverse());
        }

    }

    /**
     *  relies on the existence of the fully intialised ports, so ships may be generated at their location
     *
     * @return
     */
    public List<Ship> generateShips(int numberOfShips) {
        // initialise probabilities based on capacity of the ports
        Map<Port, Double> probabilities = new HashMap<>();

        // calculate total capacity of all ports
        double total = 0;
        for (Port p : this.portAgents.values()) { total += p.getCapacity(); }

        // create map of ports to probabilities
        for (Port p : this.portAgents.values()) {
            probabilities.put(p, ((double) p.getCapacity()) / total);

            if (p.getAgentType() == AgentType.AID_PORT) {
                for (int i = 0; i < 5; i++) {
                    try {
                        spawnShip(p);
                    } catch (NoShipSpawnedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<Ship> ships = new ArrayList<>();
        for (int i = 0; i < numberOfShips; i++) {

            // weighted random selection of port
            double randomVal = Math.random();
            for (Port p : this.portAgents.values()) {
                randomVal -= probabilities.get(p);
                if (randomVal < 0) {
                    try {
                        ships.add(spawnShip(p));
                    } catch (NoShipSpawnedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return ships;
    }

    public class NoShipSpawnedException extends Exception {}

    private Ship spawnShip(Port atPort) throws NoShipSpawnedException {
        Coordinates c = new Coordinates(
                atPort.getCoordinates().getLatitude(),
                atPort.getCoordinates().getLongitude()
        );

        // check if the port is a coastal port only serviced by offshore, if so produce a smartship™
        AgentType shiptype = AgentType.FREIGHT_SHIP;
        if (atPort instanceof CoastalPort) {
            boolean isServicedByOffshore = false;
            for (Port p : atPort.getRoutes().keySet()) {
                if (p instanceof OffshorePort) {
                    isServicedByOffshore = true;
                }
            }
            if (isServicedByOffshore) {
                shiptype = AgentType.SMART_SHIP;
            }
        }

        // Randomly select a ship capacity to be spawned
        // currently each ship will be classed as small, medium, or large
        double numTypes = 3; // so 3 types of ship
        int type = ((int) (Math.random() * numTypes));

        if (atPort.getAgentType() == AgentType.AID_PORT) {
            Ship s = new Aircraft(c, SmartShip.SMALL_CAPACITY);
            s.setState(Ship.ShipState.IDLE);
            atPort.addShip(s);
        }

        // create ship based on port type and selected ship size
        Ship s = createShip(shiptype, c, type);
        s.setState(Ship.ShipState.IDLE);
        atPort.addShip(s);

        return s;
    }

    private Ship createShip(AgentType shipType, Coordinates c, int sizeClass) throws NoShipSpawnedException {
        if (shipType == AgentType.FREIGHT_SHIP) {
            switch (sizeClass) {
                case 0: return new FreightShip(c, FreightShip.SMALL_CAPACITY);
                case 1: return new FreightShip(c, FreightShip.MEDIUM_CAPACITY);
                case 2: return new FreightShip(c, FreightShip.LARGE_CAPACITY);
            }
        } else if (shipType == AgentType.SMART_SHIP) {
            switch (sizeClass) {
                case 0: return new SmartShip(c, SmartShip.SMALL_CAPACITY);
                case 1: return new SmartShip(c, SmartShip.MEDIUM_CAPACITY);
                case 2: return new SmartShip(c, SmartShip.LARGE_CAPACITY);
            }
        }
        throw new NoShipSpawnedException();
    }

    public List<Weather> generateWeather(Map<String, Port> ports) {
        List<Weather> weatherList = new LinkedList<>();

        Weather weather1 = new Weather(new Coordinates(-18.9, 55.7), 10);
        Port p1 = ports.get("China Offshore");
        Port p2 = ports.get("Cape Town");

        weather1.addAffectedPort(p1);
        weather1.addAffectedPort(p2);

        Route orgRoute = p1.getRoutes().get(p2).get(0);
        List<Node> altNodes = new ArrayList<>();
        altNodes.addAll(orgRoute.getNodes());

        // add A node that goes through a different side of madagascar
        altNodes.set(6, new Node(IdGenerator.getId(), new Coordinates(-11.1, 43.54)));

        weather1.addAltRoute(orgRoute, new Route(altNodes, orgRoute.getWeight()));
        weatherList.add(weather1);

        return weatherList;
    }
}
