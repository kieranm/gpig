package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.vessel.Ship;
import domain.world.Node;
import domain.world.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Port extends Agent implements Carrier {

    private static final double CAPACITY_PORT_SIZE_RATIO = 0.01;

    private static final int BASE_LOAD_UNLOAD_SPEED = 1;
    private static final int SHIP_SIZE_LOADING_OFFSET = 50;

    private String name;

    private Node node;
    private Map<Port, List<Route>> routes = new HashMap<>();
    private Map<Port, Double> probabilities = new HashMap<>();

    private int cargoCapacity;
    private int cargoLoad;

    private int dockCapacity;
    private int dockLoad = 0;

    private List<Ship> managedShips;
    private List<Ship> removedShips;

    public Port(AgentType agentType, String name, Node node, int capacity, int load) {
        super(agentType, node.getCoordinates());

        this.node = node;
        this.name = name;
        this.cargoCapacity = capacity;
        this.cargoLoad = load;

        this.dockCapacity = (int)Math.rint(capacity * CAPACITY_PORT_SIZE_RATIO);

        this.managedShips = new ArrayList<>();
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

    public boolean isEmpty() { return this.cargoLoad == 0; }
    public boolean isFull() { return this.cargoLoad == this.cargoCapacity; }

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

    public void updatePort(){

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
                    this.unloadDockedShip(s);
                    break;

                // Export
                case ARRIVED:
                    s.setState(Ship.ShipState.WAITING_LOADING);
                    break;
                case WAITING_LOADING:
                    this.updateWaitingShip(s);
                    break;
                case LOADING_CARGO:
                    // do one tick of moving cargo onto docked ships
                    this.loadDockedShip(s);
                    break;

                // General management
                case IDLE:
                    // ship no longer in use by the port
                    removedShips.add(s);
                    break;
                default:
                    // if TRAVELLING nothing for port to do
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

    }

    private void unloadDockedShip(Ship ship){
        int requestedUnload = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        int amountUnloaded = ship.unloadCargo(requestedUnload);
        if (ship.isEmpty()) {
            ship.setState(Ship.ShipState.IDLE);
        }
        // notify the amount of cargo that has been consumed to the "Cargo producer"
    }

    private void loadDockedShip(Ship ship) {
        int requestedLoad = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        int amountOverCapacity = ship.loadCargo(requestedLoad);
        this.cargoLoad -= requestedLoad;
        this.cargoLoad += amountOverCapacity; // add back cargo the ship couldn't fit
        if (this.isEmpty() || ship.isFull()) {

            // Start ship on journey
            generateRoute(ship);
            this.removedShips.add(ship);
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
}
