package server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class GeoJsonSpec {

    // Mapbox Credentials
    private static final String MAPBOX_USERNAME = "kieranmch";
    private static final String MAPBOX_ACCESS_TOKEN =
            "pk.eyJ1Ijoia2llcmFubWNoIiwiYSI6ImNqMnRrNWoycjAwMXgyeHBkdDR5NTVoOWIifQ.CcpzELblchnu7wV_waTkCw";
    private String datasetID;

    private JSONArray features;

    private List<JSONObject> coastalPorts = new ArrayList<>();
    private List<JSONObject> offshorePorts = new ArrayList<>();
    private List<JSONObject> lanes = new ArrayList<>();

    public GeoJsonSpec(String datasetID) throws IOException {

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
    }

    private void verifyAttribute(JSONObject jFeature, String name) {
        if(!jFeature.getJSONObject("properties").has(name)) {
            throw new JSONException("The '" + name + "' property was missing for object with ID  -> " +
                    jFeature.getString("id"));
        }
    }

    public List<JSONObject> getCoastalPorts() {
        return coastalPorts;
    }

    public List<JSONObject> getOffshorePorts() {
        return offshorePorts;
    }

    public List<JSONObject> getLanes() {
        return lanes;
    }
}
