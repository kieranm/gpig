package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.util.PortDistanceComparator;
import domain.vessel.Ship;
import domain.world.Node;
import domain.world.Route;
import org.json.JSONObject;

import java.util.*;

public abstract class Port extends Agent implements Carrier {

    // used to derive the dock capacity (space for ships) from the export cargo capacity
    private static final double CAPACITY_PORT_SIZE_RATIO = 0.01;
    // used to determine how much cargo is produced per tick (based on capacity)
    private static final double CAPACITY_CARGO_PRODUCTION_RATIO = 0.001;
    // determines how many times cargo will be produced at the ports on initialisation
    private static final double CARGO_INITIALISATION_MULTIPLIER = 3;


    // Multiplier applied to loading/unloading, a sort of global crane speed
    private static final int BASE_LOAD_UNLOAD_SPEED = 1;
    // For every SHIP_SIZE_LOADING_OFFSET points of capacity an extra crane can be employed on a ship
    private static final int SHIP_SIZE_LOADING_OFFSET = 50;

    private String name;

    private Node node;
    private Map<Port, List<Route>> routes;
    private Map<Port, Double> probabilities = new HashMap<>();

    private int cargoCapacity;
    private int cargoLoad;

    private int dockCapacity;
    private int dockLoad = 0;

    private List<Ship> managedShips = new ArrayList<>();
    private List<Ship> removedShips = new ArrayList<>();

    public Port(AgentType agentType, String name, Node node, int capacity) {
        super(agentType, node.getCoordinates());

        this.node = node;
        this.name = name;
        this.cargoCapacity = capacity;
        this.cargoLoad = 0;
        for (int i = 0; i < CARGO_INITIALISATION_MULTIPLIER; i++) {
            produceCargo();
        }

        this.dockCapacity = (int)Math.rint(capacity * CAPACITY_PORT_SIZE_RATIO);

        this.managedShips = new ArrayList<>();

        routes = new TreeMap<>(new PortDistanceComparator(this));
    }

    //TODO is this needed?
    public Map<Port, List<Route>> getRoutes() {
        return routes;
    }

    public Node getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    public boolean isEmpty() {
        return this.cargoLoad == 0;
    }

    public boolean isFull() {
        return this.cargoLoad == this.cargoCapacity;
    }

    private boolean isSpaceToDock(Ship s) {
        return this.dockLoad + s.getLoad() <= this.dockCapacity;
    }

    public void addShip(Ship s) {
        this.managedShips.add(s);
    }

    @Override
    public int getCapacity() {
        return this.cargoCapacity;
    }

    @Override
    public int getLoad() { return this.cargoLoad; }

    // TODO The load should never be hard set to a value, only interface should be adding/subtracting?
    @Override
    public void setLoad(int load) {
        this.cargoLoad = load;
    }

    public void updatePort(int multiplier){

        // ships will be added to this list if they are no longer managed by this port
        // after this update
        removedShips = new ArrayList<>();

        for (Ship s : managedShips) {

            switch(s.getState()) {
                // Import
                case WAITING_UNLOADING:
                    // move any waiting ships into spaces made available this tick
                    this.updateWaitingShip(s);
                    break;
                case UNLOADING_CARGO:
                    // do one tick of moving cargo from docked ships
                    System.out.println(String.format("Before Unloading Ship has %d cargo at %s", s.getLoad(), this.name));
                    this.unloadDockedShip(s, multiplier);
                    System.out.println(String.format("After Unloading Ship has %d cargo at %s", s.getLoad(), this.name));
                    break;

                // Export
                case ARRIVED:
                    System.out.println(String.format("Ship has arrived at %s", this.name));
                    if (s.getLoad() > 0) {
                        s.setState(Ship.ShipState.WAITING_UNLOADING);
                    } else {
                        s.setState(Ship.ShipState.WAITING_LOADING);
                    }
                    break;
                case WAITING_LOADING:
                    this.updateWaitingShip(s);
                    break;
                case LOADING_CARGO:
                    // do one tick of moving cargo onto docked ships
                    System.out.println(String.format("Before loading %s has %d cargo", this.name, this.cargoLoad));
                    this.loadDockedShip(s, multiplier);
                    System.out.println(String.format("after loading %s has %d cargo", this.name, this.cargoLoad));
                    break;

                default:
                    // if TRAVELLING or IDLE nothing for port to do
                    break;
            }
        }

        // remove unmanaged ships
        for (Ship s : removedShips) {
            managedShips.remove(s);
        }

        // if the port has outbound cargo, but not enough ships to fulfil the load
        // currently at port. Bid for additional ships to travel to the dock
        this.bidForShips();

        produceCargo();
    }

    private void produceCargo() {
        int newCargo = ((int) (CAPACITY_CARGO_PRODUCTION_RATIO * ((double) this.cargoCapacity)));
        if (this.cargoCapacity < this.cargoLoad + newCargo) {
            // If would generate more than the capacity of the port, limit the amount generated
            newCargo = this.cargoLoad + newCargo - this.cargoCapacity;
        }
        this.cargoLoad += newCargo;
    }

    private void unloadDockedShip(Ship ship, int multiplier){
        int requestedUnload = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        requestedUnload *= multiplier;
        int amountUnloaded = ship.unloadCargo(requestedUnload);
        this.stats.addDeliveredCargo(amountUnloaded);
        if (ship.isEmpty()) {
            ship.setState(Ship.ShipState.IDLE);
        }

        // TODO notify the amount of cargo that has been consumed to the "Cargo producer"
    }

    private void loadDockedShip(Ship ship, int multiplier) {
        int requestedLoad = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        requestedLoad *= multiplier;
        int amountOverCapacity = ship.loadCargo(requestedLoad);

        this.cargoLoad -= requestedLoad;
        this.cargoLoad += amountOverCapacity; // add back cargo the ship couldn't fit
        if (this.isEmpty() || ship.isFull()) {

            // Start ship on journey
            generateRoute(ship);
        }
    }

    private void updateWaitingShip(Ship ship){

        if (isSpaceToDock(ship)) {
            this.dockLoad += ship.getCapacity();

            if (ship.getState() == Ship.ShipState.WAITING_LOADING) {
                ship.setState(Ship.ShipState.LOADING_CARGO);
            } else { // Ship state == WAITING_UNLOADING
                ship.setState(Ship.ShipState.UNLOADING_CARGO);
            }
        }
    }

    private void bidForShips() {

        // count the amount of cargo currently ready to be exported from the port
        int maximumMovableCargo = 0;
        for (Ship s : this.managedShips) {
            if (s.getState() == Ship.ShipState.WAITING_LOADING ||
                    s.getState() == Ship.ShipState.LOADING_CARGO ||
                    s.getState() == Ship.ShipState.TRAVELING) {
                maximumMovableCargo += s.getCapacity();
            }
        }

        // if more ships are required to send existing cargo, request more ships
        int cargoLeftToBeMoved = this.cargoLoad - maximumMovableCargo;
        if (cargoLeftToBeMoved > 0) {

            // check if any ships currently at this port can be used
            if (!this.findBidder(this, cargoLeftToBeMoved)) {

                //  otherwise check the other ports in this network, starting at the closest port
                for (Port port : this.routes.keySet()) {
                    if (findBidder(port, cargoLeftToBeMoved)) {
                        return;
                    }
                }
            }
        }
    }

    /**
     *
     * @param p
     * @return whether the search for a bidder was successful
     */
    private boolean findBidder(Port p, int cargoToBeMoved) {
        // Check for any idle ships in this port
        List<Integer> bids = new ArrayList<>();
        for (Ship s : p.getManagedShips()) {
            bids.add(s.getBid(cargoToBeMoved));
        }
        int bestBidIndex = 0;
        int bestBid = 0;
        for (int i = 0; i < bids.size(); i++) {
            if (bids.get(i) > bestBid) {
                bestBid = bids.get(i);
                bestBidIndex = i;
            }
        }
        if (bestBid > 0) {

            if (this.equals(p)) { // if searching from existing port set the ship state to arrived begin
                this.managedShips.get(bestBidIndex).setState(Ship.ShipState.ARRIVED);
                return true;
            }

            // search the routes from the remote port for a valid path back to here
            for (Route route : p.getRoutes().get(this)) {
                if (route.isActive()) {
                    // transfer control of ship and assign route
                    Ship newShip = p.getManagedShips().remove(bestBidIndex);
                    this.managedShips.add(newShip);
                    newShip.assignRoute(route.getNodes());

                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     *
     * @return a randomly selected route from this port to one of its destinations
     */
    public void generateRoute(Ship s) {
        double randomVal = Math.random();
        // reduce the random value by the probability weight until 0 is reached
        for (Port destination : this.routes.keySet()) {

            randomVal -= this.probabilities.get(destination);
            if (randomVal <= 0) {

                for (Route route : this.routes.get(destination)) {
                    if (route.isActive()) {
                        destination.addShip(s);
                        s.assignRoute(route.getNodes());
                        this.removedShips.add(s);
                    }
                }
            }
        }
    }

    /**
     *  Used to register a new route from the port to the given destination
     *
     * @param destination
     * @param route
     */
    public void addRoute(Port destination, Route route) {
        if (this.routes.get(destination) == null) {
            List<Route> newRoutes = new ArrayList<>();
            newRoutes.add(route);
            this.routes.put(destination, newRoutes);

            updateProbabilities();
        } else {
            this.routes.get(destination).add(route);
        }
    }

    /**
     * invoked on each additional destination added to the port (via addRoute)
     */
    private void updateProbabilities() {
        double total = 0.0;
        for (List<Route> routesToDestination : this.routes.values()) {
            // only add the first weight for consideration in random selection
            // multiple routes are only for simulation of congestion/weather
            total += routesToDestination.get(0).getWeight();
        }
        for (Port destination : this.routes.keySet()) {
            double newprob = ((double) this.routes.get(destination).get(0).getWeight()) / total;
            this.probabilities.put(destination, newprob);
        }
    }

    public List<Ship> getManagedShips() {
        return managedShips;
    }

    private long getContainerLoad() {
        double containerLoad = (double) cargoLoad / (double) cargoCapacity;
        return Math.round(containerLoad * 10.0);
    }

    private long getDockLoad() {
        double dl = (double) dockLoad / (double) dockCapacity;
        return Math.round(dl * 10.0);
    }

    private long getQueueLoad() {
        int total = managedShips.size();
        if (total == 0) return 0;
        long queueing = managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.WAITING_UNLOADING)
                .count();
        double prop = (double) queueing / (double) total;
        return Math.round(prop * 10.0);
    }

    private long getThroughput() {
        return 0l;
    }

    @Override
    public JSONObject toJSON() {
        Map<String, Long> m = new HashMap<>(4);
        m.put("NW", getContainerLoad());
        m.put("NE", getDockLoad());
        m.put("SW", getQueueLoad());
        m.put("SE", getThroughput());
        return super.toJSON().put("statistics", m);
    }

}
