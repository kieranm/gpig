package domain.port;

import java.util.*;

import domain.Agent;
import domain.vessel.Ship;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.world.Node;
import domain.world.Route;

public abstract class Port extends Agent implements Carrier {

    private static final double CAPACITY_PORT_SIZE_RATIO = 0.01;
    private String name;

    private int capacity;
    private int load;
    private Node node;
    private Map<Port, List<Route>> routes;
    private Map<Port, Double> probabilities;
    private int cargoMoveSpeed; // how many cargo moves happen per tick with speed x1
    private int dockCapacity;

    private int dockLoad = 0;
    private List<Ship> dockedShips = new LinkedList<>();
    private Queue<Ship> waitingShips = new LinkedList<>();

    public Port(AgentType agentType, String name, Node node, Map<Port, List<Route>> routes,
                Map<Port, Double> probabilities, int capacity, int load, int cargoMoveSpeed){
        super(agentType, node.getCoordinates());

        this.node = node;
        this.routes = routes;
        this.probabilities = probabilities;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
        this.cargoMoveSpeed = cargoMoveSpeed;

        this.dockCapacity = (int)Math.rint(capacity * CAPACITY_PORT_SIZE_RATIO);
    }

    public Node getNode() { return node; }
    public String getName() {
        return name;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public int getLoad() { return this.load; }

    @Override
    public boolean isEmpty() { return this.load == 0; }

    @Override
    public boolean isFull() { return this.load == this.capacity; }

    @Override
    // Returns number of containers that are over capacity
    public int loadContainers(int count){
        int loadDifference = this.load + count - this.capacity;
        this.load = loadDifference > 0 ? this.capacity : this.load + count;

        return loadDifference > 0 ? loadDifference : 0;
    }

    @Override
    // Returns number of containers unloaded
    public int unloadContainers(int count) {
        int loadDifference = this.load - count;
        this.load = loadDifference < 0 ? 0 : loadDifference;

        return loadDifference < 0 ? count + loadDifference : count;
    }

    public void DockShip(Ship ship){
        if(this.waitingShips.size() == 0 && this.dockLoad + ship.getCapacity() <= this.dockCapacity)
        {
            ship.setState(Ship.ShipState.UNLOADING_CARGO);
            this.dockedShips.add(ship);
        }

        ship.setState(Ship.ShipState.WAITING);
        this.waitingShips.add(ship);
    }

    public void updateDocks(int simulationSpeed){
        this.updateWaitingShips();

        this.unloadDockedShips(this.cargoMoveSpeed * simulationSpeed);
        this.loadDockedShips(this.cargoMoveSpeed * simulationSpeed);

        this.releaseDockedShips();
    }

    private void updateWaitingShips(){
        while(this.dockLoad + this.waitingShips.peek().getCapacity() <= this.dockCapacity) {
            Ship ship = this.waitingShips.remove();
            ship.setState(Ship.ShipState.UNLOADING_CARGO);
            this.dockedShips.add(ship);
        }
    }

    private void unloadDockedShips(int unloadSpeed){
        for(Ship ship : this.dockedShips) {
            if(ship.getState() != Ship.ShipState.UNLOADING_CARGO) continue;

            if(ship.isEmpty()) ship.setState(Ship.ShipState.LOADING_CARGO);
        }
    }

    private void loadDockedShips(int loadSpeed) {
        for(Ship ship : this.dockedShips) {
            if(ship.getState() != Ship.ShipState.LOADING_CARGO) continue;

        }

    }

    private Ship assignRandomDestination(Ship ship){
        Random random = new Random();

        double probSum = 0;
        double randomDouble = random.nextDouble();

        for (Map.Entry<Port, Double> entry : this.probabilities.entrySet()) {
            probSum += entry.getValue();

            if(probSum >= randomDouble) {
                List<Route> routes = this.routes.get(entry.getKey());
                int randomIndex = random.nextInt(routes.size());
                // TODO set route properly
                //ship.setRoute(routes.get(randomIndex));
                ship.SetDestinationPort(entry.getKey());
                break;
            }
        }

        return ship;
    }

    private void releaseDockedShips(){
        // Iterating from the end of the list to avoid indexOutOfRange
        for(int i = this.dockedShips.size() - 1; i > 0; i--) {
            Ship ship = this.dockedShips.get(i);
            if(ship.getState() != Ship.ShipState.LOADING_CARGO && ship.isFull()) continue;

            this.dockedShips.remove(i);

            ship = this.assignRandomDestination(ship);
            ship.startRoute();
        }
    }
}
