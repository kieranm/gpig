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

    // used to determine how much cargo is produced per tick (based on capacity)
    static final double CAPACITY_CARGO_PRODUCTION_RATIO = 0.001;
    // determines how many times cargo will be produced at the ports on initialisation
    private static final double CARGO_INITIALISATION_MULTIPLIER = 10;

    // Multiplier applied to loading/unloading, a sort of global crane speed
    static final int BASE_LOAD_UNLOAD_SPEED = 10;

    // For every SHIP_SIZE_LOADING_OFFSET points of capacity an extra crane can be employed on a ship
    static final int SHIP_SIZE_LOADING_OFFSET = 50;

    private String name;

    private Node node;
    private Map<Port, List<Route>> routes;
    private Map<Port, Double> probabilities = new HashMap<>();

    int cargoCapacity;
    int cargoLoad;

    private int dockCapacity;
    int dockLoad = 0;

    private Set<Ship> managedShips;
    private Set<Ship> removedShips;

    public Port(AgentType agentType, String name, Node node, int capacity, int dock_capacity) {
        super(agentType, node.getCoordinates());

        this.node = node;
        this.name = name;
        this.cargoCapacity = capacity;
        this.cargoLoad = 0;
        for (int i = 0; i < CARGO_INITIALISATION_MULTIPLIER; i++) {
            produceCargo();
        }

        this.dockCapacity = dock_capacity;

        this.managedShips = new HashSet<>();
        this.removedShips = new HashSet<>();

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
        return this.dockLoad + s.getCapacity() <= this.dockCapacity;
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
        removedShips = new HashSet<>();

        boolean isFrontOfQueue = true; // flag is unset after the first waiting ship is addressed (FIFO)
        // TODO cheap "collaboration" if we drop the fifo queue for first fit in OceanX approach

        for (Ship s : managedShips) {

            switch(s.getState()) {
                // Import
                case WAITING_UNLOADING:
                    // move any waiting ships into spaces made available this tick
                    if (isFrontOfQueue) {
                        // if ship is serviced, allow the next ship to be considered (function returns true)
                        // if ship still waiting function returns false.
                        isFrontOfQueue = this.updateWaitingShip(s);
                    }
                    break;
                case UNLOADING_CARGO:
                    // do one tick of moving cargo from docked ships
                    this.unloadDockedShip(s, multiplier);
                    break;

                // Export
                case ARRIVED:
//                    System.out.println(String.format("Ship has arrived at %s", this.name));
                    if (s.getLoad() > 0) {
                        s.setState(Ship.ShipState.WAITING_UNLOADING);
                    } else {
                        s.setState(Ship.ShipState.WAITING_LOADING);
                    }
                    break;
                case WAITING_LOADING:
                    if (isFrontOfQueue) {
                        // if ship is serviced, allow the next ship to be considered (function returns true)
                        // if ship still waiting function returns false.
                        isFrontOfQueue = this.updateWaitingShip(s);
                    }
                    break;
                case LOADING_CARGO:
                    // do one tick of moving cargo onto docked ships
//                    System.out.println(String.format("Before loading %s has %d cargo", this.name, this.cargoLoad));
                    this.loadDockedShip(s, multiplier);
//                    System.out.println(String.format("after loading %s has %d cargo", this.name, this.cargoLoad));
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

    abstract void produceCargo();

    abstract void unloadDockedShip(Ship ship, int multiplier);

    private void loadDockedShip(Ship ship, int multiplier) {
        int requestedLoad = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        requestedLoad *= multiplier;
        requestedLoad = Math.min(requestedLoad, this.cargoLoad);
        int amountLoaded = ship.loadCargo(requestedLoad);

        this.cargoLoad -= amountLoaded;
        if (this.isEmpty() || ship.isFull()) {

            this.dockLoad -= ship.getCapacity();
            // Start ship on journey
            generateRoute(ship);
        }
    }

    /**
     *  if ship is serviced, the next ship will be at the front of the queue (function returns true)
     *  if ship still waiting function returns false to stop subsequent ships being considered for docking.
     * @param ship
     * @return
     */
    private boolean updateWaitingShip(Ship ship){

        if (isSpaceToDock(ship)) {
            this.dockLoad += ship.getCapacity();

            if (ship.getState() == Ship.ShipState.WAITING_LOADING) {
                ship.setState(Ship.ShipState.LOADING_CARGO);
            } else { // Ship state == WAITING_UNLOADING
                ship.setState(Ship.ShipState.UNLOADING_CARGO);
            }

            return true;
        }
        return false;
    }

    private void bidForShips() {

        // count the amount of cargo currently ready to be exported from the port
        int maximumMovableCargo = 0;
        for (Ship s : this.managedShips) {
            if (s.getState() == Ship.ShipState.WAITING_LOADING ||
                    s.getState() == Ship.ShipState.LOADING_CARGO ||
                    s.getState() == Ship.ShipState.TRAVELING) {
                maximumMovableCargo += s.getCapacity();

                // TODO subtract the current load from ships loading cargo
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

        boolean requestSmartShip = false;
        if (this.getAgentType() == AgentType.LAND_PORT) {
            // we are a land port in a network serviced by smart ships, only request smart ships for cargo
            requestSmartShip = this.routes.keySet().stream().anyMatch(otherPort ->
                    otherPort.getAgentType() == AgentType.SMART_PORT);
        } else { // this.getAgentType() == AgentType.SMART_PORT
            requestSmartShip = p.getAgentType() == AgentType.LAND_PORT;
        }

        Map<Ship, Integer> bids = new HashMap<>(p.getManagedShips().size());
        for (Ship s : p.getManagedShips()) {
            if (s.getCapacity() > this.dockCapacity) {
                bids.put(s, null);
            } else {
                bids.put(s, s.getBid(cargoToBeMoved, requestSmartShip));
            }
        }
        Map.Entry<Ship, Integer> best = null;
        for (Map.Entry<Ship, Integer> si : bids.entrySet()) {
            if (best == null || (si.getValue() != null && best.getValue() != null && si.getValue() > best.getValue())) {
                best = si;
            }
        }
        if (best == null) {
            return false;
        }
        Integer bestBid = best.getValue();
        Ship bestShip = best.getKey();
        if (bestBid != null && bestBid > 0) {

            if (this.equals(p)) { // if searching from existing port set the ship state to arrived begin
                bestShip.setState(Ship.ShipState.ARRIVED);
                return true;
            }

            // search the routes from the remote port for a valid path back to here
            for (Route route : p.getRoutes().get(this)) {
                if (route.isActive()) {
                    // transfer control of ship and assign route
                    p.getManagedShips().remove(bestShip);
                    this.managedShips.add(bestShip);
                    bestShip.assignRoute(route.getNodes());

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

        Map<Port, Double> validProbabilities = new HashMap<>();
        double total = 0.0;
        for (Port p : this.routes.keySet()) {
            if (s.getCapacity() <= p.getCapacity()) {
                validProbabilities.put(p, this.probabilities.get(p));
                total += this.probabilities.get(p);
            }
        }
        double randomVal = Math.random() * total;
        // reduce the random value by the probability weight until 0 is reached
        for (Port destination : validProbabilities.keySet()) {

            randomVal -= validProbabilities.get(destination);
            if (randomVal <= 0) {

                for (Route route : this.routes.get(destination)) {
                    if (route.isActive()) {
                        destination.addShip(s);
                        s.assignRoute(route.getNodes());
                        this.removedShips.add(s);
                        return;
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

    public Set<Ship> getManagedShips() {
        return managedShips;
    }

    private long calculateContainerLoadStatistics() {
        double containerLoad = (double) cargoLoad / (double) cargoCapacity;
        return Math.round(containerLoad * 1000.0);
    }

    private long calculateDockLoadStatistics() {
        double dl = (double) dockLoad / (double) dockCapacity;
        return Math.round(dl * 1000.0);
    }

    private long calculateQueueLoadStatistics() {
        int total = managedShips.size();
        if (total == 0) return 0;

        // remove "idle" from total, more of a quirk of our system than a realistic state to be in
        // or implement some "smart" planning to avoid stats looking bad for OceanX case
        total -= managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.IDLE)
                .count();

        // calculate how many ships are currently in one of the waiting states
        long queueing = managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.WAITING_UNLOADING)
                .count();
        queueing += managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.WAITING_LOADING)
                .count();

        double prop = (double) queueing / (double) total;
        return Math.round(prop * 1000.0);
    }

    private long calculateIdleShips() {
        int total = managedShips.size();
        if (total == 0) return 0;

        long idle = this.managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.IDLE)
                .count();

        return idle;
    }

    @Override
    public JSONObject toJSON() {
        Map<String, JSONObject> m = new HashMap<>(4);
        m.put("NW", new JSONObject().put("name", "Container Load").put("value", calculateContainerLoadStatistics()));
        m.put("NE", new JSONObject().put("name", "Dock Load").put("value", calculateDockLoadStatistics()));
        m.put("SW", new JSONObject().put("name", "Queue Load").put("value", calculateQueueLoadStatistics()));
        m.put("SE", new JSONObject().put("name", "Idle Ships").put("value", calculateIdleShips()));
        JSONObject debugging = new JSONObject();
        debugging.put("Travelling", this.managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.TRAVELING)
                .count());
        debugging.put("Queueing", this.managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.WAITING_LOADING ||
                            s.getState() == Ship.ShipState.WAITING_UNLOADING)
                .count());
        debugging.put("Idle", this.managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.IDLE)
                .count());
        debugging.put("Utilising Port", this.managedShips.stream()
                .filter(s -> s.getState() == Ship.ShipState.LOADING_CARGO ||
                        s.getState() == Ship.ShipState.UNLOADING_CARGO)
                .count());
        return super.toJSON()
                .put("name", this.name)
                .put("statistics", m)
                .put("debug", debugging);
    }

}
