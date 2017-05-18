package domain.vessel;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.util.Coordinates;
import domain.world.Node;
import org.json.JSONObject;

import java.util.List;

/**
 * @author Oliver Lea
 */
public abstract class Ship extends Agent implements Carrier {

    private int capacity;
    private int load;

    private static final double DISTANCE_PER_TICK_MULTIPLIER = 0.1;

    private List<Node> route;
    private Node next;
    private Coordinates positionUpdateVector;

    private ShipState state = ShipState.IDLE;

    public enum ShipState{
        IDLE,
        WAITING,
        UNLOADING_CARGO,
        LOADING_CARGO,
        TRAVELING,
    }

    public Ship(AgentType agentType, Coordinates initialLoc, int capacity, int load) {
        super(agentType, initialLoc);
        this.capacity = capacity;
        this.load = load;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setState(ShipState state){ this.state = state; }

    public ShipState getState() { return this.state; }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getLoad() {
        return load;
    }

    public void followRoute() {

        // check if we have reached the next waypoint
        // TODO figure out what a sensible distance is to be considered "on" the next waypoint
        if (hasReachedPoint()) {

            // if the end of the route has been reached attempt to dock
            // else set the next route point
            if (routeEndReached()) {
               // TODO -- add docking
            } else {
                nextRouteStop();
            }
            // calculate new vector toward next waypoint
            calculatePositionUpdateVector();
        }

        // move toward next
        this.setCoordinates(this.getCoordinates().add(this.positionUpdateVector));
        if (this.getCoordinates().distance(this.next.getCoordinates()) < this.positionUpdateVector.length()) {
            this.setCoordinates(new Coordinates(this.next.getCoordinates()));
        }
    }

    private boolean hasReachedPoint() {
        return this.next.getCoordinates().equals(this.getCoordinates());
    }

    private boolean routeEndReached() {
        return this.route.indexOf(this.next) == this.route.size()-1;
    }

    private void calculatePositionUpdateVector() {
        // calculate distance include direction in vector
        double xdiff = this.next.getCoordinates().getLatitude() - this.getCoordinates().getLatitude();
        double ydiff = this.next.getCoordinates().getLongitude() - this.getCoordinates().getLongitude();

        double length = this.getCoordinates().distance(this.next.getCoordinates());
        this.positionUpdateVector = new Coordinates(xdiff/length, ydiff/length);
        this.positionUpdateVector.mul(DISTANCE_PER_TICK_MULTIPLIER); // TODO figure out a good amount of travel per tick
    }

    public void nextRouteStop() {
        int current = this.route.indexOf(this.next);
        if (current != this.route.size()-1) {
            this.next = this.route.get(current+1);
        }
    }

    public void startRoute()
    {
        this.state = ShipState.TRAVELING;
        this.next = route.get(1);
        calculatePositionUpdateVector();
    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON()
                .put("load", this.load)
                .put("capacity", this.capacity);
    }

    @Override
    public void setLoad(int load) {
        this.load = load;
    }
}
