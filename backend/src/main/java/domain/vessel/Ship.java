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

    private Integer waitingTime = 0;

    public enum ShipState {
        IDLE,
        WAITING_UNLOADING,
        UNLOADING_CARGO,
        WAITING_LOADING,
        LOADING_CARGO,
        TRAVELING,
        ARRIVED,
    }

    public Ship(AgentType agentType, Coordinates initialLoc, int capacity) {
        super(agentType, initialLoc);
        this.capacity = capacity;
        this.load = 0;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setState(ShipState state) {
        // set the waiting timer to 0 if moving into a waiting sate
        if (state == ShipState.WAITING_LOADING || state == ShipState.WAITING_UNLOADING) {
            this.waitingTime = 0;
        }
        this.state = state;
    }

    public ShipState getState() { return this.state; }

    public boolean isEmpty() { return this.load == 0; }
    public boolean isFull() { return this.load == this.capacity; }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getLoad() {
        return load;
    }

    public Integer getBid(int cargoLeftToBeMoved, boolean requestingSmartShip) {

        if (this.getAgentType() == AgentType.FREIGHT_SHIP && requestingSmartShip) {
            return null;
        }
        if (this.getAgentType() == AgentType.SMART_SHIP && !requestingSmartShip) {
            return null;
        }

        if (this.state == ShipState.IDLE) {
            if (this.capacity >= cargoLeftToBeMoved) {
                return cargoLeftToBeMoved - (this.capacity - cargoLeftToBeMoved);
            }
            return this.capacity;
        }
        return null;
    }

    public void followRoute(int multiplier) {

        // check if we have reached the next waypoint
        // TODO figure out what a sensible distance is to be considered "on" the next waypoint
        if (hasReachedPoint()) {

            // if the end of the route has been reached attempt to dock
            // else set the next route point
            if (routeEndReached()) {
                // Ship moves to the arrived state waiting to be added to the destinations port queue
                this.setState(ShipState.ARRIVED);
                return;
            } else {
                nextRouteStop();
            }
            // calculate new vector toward next waypoint
            calculatePositionUpdateVector();
        }

        // move toward next
        this.setCoordinates(this.getCoordinates().add(this.positionUpdateVector.mul((double) multiplier)));
        if (this.getCoordinates().distance(this.next.getCoordinates()) < this.positionUpdateVector.mul((double) multiplier).length()) {
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
        this.positionUpdateVector = this.positionUpdateVector.mul(DISTANCE_PER_TICK_MULTIPLIER); // TODO figure out a good amount of travel per tick
    }

    public void nextRouteStop() {
        int current = this.route.indexOf(this.next);
        if (current != this.route.size()-1) {
            this.next = this.route.get(current+1);

            // if the next is on the other side of the globe, jump to the next point, this will skip the distance
            // between the waypoints but simplifies the wrap around edge case
            if (this.getCoordinates().getLongitude() > 175.0 && this.next.getCoordinates().getLongitude() < -175.0
                    || this.getCoordinates().getLongitude() < -175.0 && this.next.getCoordinates().getLongitude() > 175.0) {
                Coordinates c = new Coordinates(this.next.getCoordinates().getLatitude(), this.next.getCoordinates().getLongitude());
                this.setCoordinates(c);
                nextRouteStop();
            }
        }
    }

    public void assignRoute(List<Node> route) {
        this.state = ShipState.TRAVELING;
        this.route = route;
        this.next = route.get(1);
        calculatePositionUpdateVector();
    }

    public int unloadCargo(int requestedAmount) {
        int amountRemoved = Math.min(requestedAmount, this.getLoad());
        this.load -= amountRemoved;
        return amountRemoved;
    }

    public int loadCargo(int requestedAmount) {
        int amountLoaded = Math.min(requestedAmount, this.capacity - this.load);
        this.load += amountLoaded;
        return amountLoaded;
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

    public Integer getWaitingTime() {
        return waitingTime;
    }

    public void addWaitingTime(int multiplier) {
        // only add time if in a waiting stage (should already be caught by state machine in tick function)
        if (this.state == ShipState.WAITING_LOADING || this.state == ShipState.WAITING_UNLOADING) {
            this.waitingTime += multiplier;
        }
    }
}
