package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.vessel.Ship;
import domain.world.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Port extends Agent implements Carrier {

    private static final double CAPACITY_PORT_SIZE_RATIO = 0.01;
    private String name;

    private int capacity;
    private int load;

    private Node node;

    private int dockCapacity;
    private int dockLoad = 0;
    private List<Ship> dockedShips;
    private List<Ship> waitingShips;

    public Port(AgentType agentType, String name, Node node, int capacity, int load) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
        this.dockCapacity = (int)Math.rint(capacity * CAPACITY_PORT_SIZE_RATIO);
        this.dockedShips = new ArrayList<>();
        this.waitingShips = new ArrayList<>();
    }

    public Node getNode() {
        return node;
    }

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
    public void setLoad(int load) {
        this.load = load;
    }

    public void attemptDock(Ship s) {
        boolean shouldWait = false;
        if (this.waitingShips.isEmpty()) {
            if (isSpaceToDock(s)) {
                dock(s);
            } else {
                shouldWait = true;
            }
        } else {
            shouldWait = true;
        }

        if (shouldWait) {
            s.setState(Ship.ShipState.WAITING);
            this.waitingShips.add(s);
        }
    }

    private void dock(Ship s) {
        s.setState(Ship.ShipState.UNLOADING_CARGO);
        this.dockedShips.add(s);
    }

    private boolean isSpaceToDock(Ship s) {
        return this.dockLoad + s.getCapacity() <= this.dockCapacity;
    }

    public void updateDocks(int simulationSpeed){
        this.updateWaitingShips();

        // TODO - Scale by the global offload rate
        this.unloadDockedShips(simulationSpeed);
        this.loadDockedShips(simulationSpeed);

        this.releaseDockedShips();
    }

    private void updateWaitingShips(){
        if (this.waitingShips.isEmpty()) {
            return;
        }
        List<Integer> removedIndexes = new ArrayList<>();
        for (int i = 0; i < this.waitingShips.size(); ++i) {
            Ship s = this.waitingShips.get(i);
            if (this.isSpaceToDock(s)) {
                removedIndexes.add(i);
                dock(s);
            } else {
                return;
            }
        }
        Collections.reverse(removedIndexes);
        for (Integer removed : removedIndexes) {
            this.waitingShips.remove(removed.intValue());
        }
    }

    private void unloadDockedShips(int unloadSpeed){
        // TODO - actually unload
        for (Ship ship : this.dockedShips) {
            if (ship.getState() != Ship.ShipState.UNLOADING_CARGO) {
                continue;
            }
            if (ship.getLoad() == 0) {
                ship.setState(Ship.ShipState.LOADING_CARGO);
            }
        }
    }

    private void loadDockedShips(int loadSpeed) {
        for (Ship ship : this.dockedShips) {
            if (ship.getState() != Ship.ShipState.LOADING_CARGO) {
                continue;
            }
            // TODO actually load
        }

    }

    /**
     * Liam and Kieran TODO when you add probabilities and routes
     */
//    private Ship assignRandomDestination(Ship ship){
//        Random random = new Random();
//
//        double probSum = 0;
//        double randomDouble = random.nextDouble();
//
//        for (Map.Entry<Port, Double> entry : this.probabilities.entrySet()) {
//            probSum += entry.getValue();
//
//            if(probSum >= randomDouble) {
//                List<Route> routes = this.routes.get(entry.getKey());
//                int randomIndex = random.nextInt(routes.size());
//                // TODO set route properly
//                //ship.setRoute(routes.get(randomIndex));
//                break;
//            }
//        }
//
//        return ship;
//    }

    private void releaseDockedShips() {
        // Iterating from the end of the list to avoid indexOutOfRange
        for (int i = this.dockedShips.size() - 1; i >= 0; i--) {
            Ship ship = this.dockedShips.get(i);
            if (ship.getState() == Ship.ShipState.LOADING_CARGO && ship.getLoad() == ship.getCapacity()) {
                this.dockedShips.remove(i);
                // TODO
//                ship = this.assignRandomDestination(ship);
                ship.startRoute();
            }
        }
    }
}
