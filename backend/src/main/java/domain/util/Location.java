package domain.util;

import org.json.JSONObject;

/**
 * @author Oliver Lea
 */
public class Location implements JSONable {

    private double longitude;
    private double latitude;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("longitude", longitude)
                .put("latitude", latitude);

    }
}
