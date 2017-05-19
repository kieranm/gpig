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

    public Coordinates(Coordinates coordinates) {
        this.latitude = coordinates.getLatitude();
        this.longitude = coordinates.getLongitude();
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

    public String toString() {
        return String.format("lat %f long %f", latitude, longitude);
    }

    /**
     *  In place add of two coordinates (alternatively could coordinates extend a 2d vector)
     * @param c
     * @return
     */
    public Coordinates add(Coordinates c) {
        return this.add(c.getLatitude(), c.getLongitude());
    }
    public Coordinates add(double latitude, double longitude) {
        return new Coordinates(this.latitude + latitude, this.longitude + longitude);
    }

    /**
     *  In place multiplication (alternatively could coordinates extend a 2d vector)
     * @param c
     * @return
     */
    public Coordinates mul(Coordinates c) {
        return this.mul(c.getLatitude(), c.getLongitude());
    }

    public Coordinates mul(double scalar) {
        return this.mul(scalar, scalar);
    }
    public Coordinates mul(double latitude, double longitude) {
        return new Coordinates(this.latitude * latitude, this.longitude * longitude);
    }

    public double length() {
        return Math.sqrt((this.latitude*this.latitude) + (this.longitude*this.longitude));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coordinates) {
            Coordinates c = ((Coordinates) o);
            return this.latitude == c.getLatitude() &&
                    this.longitude == c.getLongitude();
        }
        return false;
    }

    public double distance(Coordinates c) {

        double xdiff = 0;
        if (c.getLatitude() > this.getLatitude()) {
            xdiff = c.getLatitude() - this.getLatitude();
        } else {
            xdiff = this.getLatitude() - c.getLatitude();
        }

        double ydiff = 0;
        if (c.getLongitude() > this.getLongitude()) {
            ydiff = c.getLongitude() - this.getLongitude();
        } else {
            ydiff = this.getLongitude() - c.getLongitude();
        }

        return Math.sqrt((xdiff*xdiff) + (ydiff*ydiff));
    }
}
