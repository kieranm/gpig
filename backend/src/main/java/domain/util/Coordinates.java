package domain.util;

import org.json.JSONObject;
import utils.JSONable;

/**
 * @author Oliver Lea
 */
public class Coordinates implements JSONable {

    private double latitude;
    private double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("latitude", latitude)
                .put("longitude", longitude);
    }
}
