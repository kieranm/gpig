package server;

import domain.port.CoastalPort;
import domain.port.Port;
import domain.util.Coordinates;
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
            latitude = (double)jPort.getJSONObject("geometry").getJSONArray("coordinates").get(1);
            longitude = (double)jPort.getJSONObject("geometry").getJSONArray("coordinates").get(0);
            portNode = this.createNode(latitude, longitude);
            port = new CoastalPort(
                    name,
                    portNode,
                    jPort.getJSONObject("properties").getInt("capacity"),
                    0
            );

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

            // Create a route and assign it to ports
            route = new Route(nodes, weight);
            startPort.addRoute(endPort, route);
            endPort.addRoute(startPort, route.reverse());
        }

    }

    Map<String, Port> getPorts() {
        return this.portAgents;
    }

}
