package domain.port;

import java.util.*;

import control.Dispatcher;
import domain.Agent;
import domain.vessel.Ship;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public abstract class Port extends Agent implements Carrier {

    private String name;

    private int capacity;
    private int load;
    private Node node;
    private int cargoMoveSpeed; // how many cargo moves happen per tick with speed x1

    private Dock[] docks;
    private Queue<Ship> waitingShips = new LinkedList<>();

    private Dispatcher dispacher;

    private Node portNode;

    public Port(AgentType agentType, String name,
                Node node, int capacity, int load, int portSize, int cargoMoveSpeed, Dispatcher dispacher) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
        this.cargoMoveSpeed = cargoMoveSpeed;
        this.dispacher = dispacher;

        this.docks = new Dock[portSize];
        for(int i = 0; i < portSize; i++) this.docks[i] = new Dock();
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
    public int loadContainers(int count)
    {
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

    public void updateDocks(int simulationSpeed)
    {
        this.updateWaitingShips();

        this.unloadDockedShips(this.cargoMoveSpeed * simulationSpeed);
        this.loadDockedShips(this.cargoMoveSpeed * simulationSpeed);

        this.assignNewOrdersForDockedShips();
        this.releaseDockedShips();
    }

    private void updateWaitingShips()
    {
        // Docks waiting ships if slot is free
        for (Dock dock : this.docks)
            if (dock.isEmtpy())
            {
                if (this.waitingShips.size() == 0) return;
                dock.moorShip(this.waitingShips.remove());
            }

    }

    private void unloadDockedShips(int unloadSpeed)
    {
        // Unloads all docked ships
        for(Dock dock: this.docks)
        {
            if(dock.isEmtpy() || dock.state != Dock.DockState.UNLOADING_OLD_CARGO) continue;

            int unloadedCount = dock.ship.unloadContainers(unloadSpeed);
            int overCapacityCount = this.loadContainers(unloadedCount);

            // Load them back if over capacity
            if(overCapacityCount > 0) dock.ship.loadContainers(overCapacityCount);

            if(dock.ship.isEmpty()) dock.state = Dock.DockState.READY_FOR_NEW_ORDERS;
        }
    }

    private void loadDockedShips(int loadSpeed)
    {
        for(Dock dock: this.docks)
        {
            if(dock.isEmtpy() || dock.state != Dock.DockState.LOADING_NEW_CARGO) continue;

            int containerCount = loadSpeed > dock.cargoToLoad ? loadSpeed : dock.cargoToLoad;

            int unloadedContainers = this.unloadContainers(containerCount);
            int overCapacityCount = dock.ship.loadContainers(unloadedContainers);
            this.loadContainers(overCapacityCount);

            dock.cargoToLoad -= unloadedContainers - overCapacityCount;

            if(dock.cargoToLoad == 0 || dock.ship.isFull()) dock.state = Dock.DockState.READY_FOR_RELEASE;
        }
    }

    private void assignNewOrdersForDockedShips()
    {
        for(Dock dock: this.docks)
        {
            if(dock.isEmtpy() || dock.state != Dock.DockState.READY_FOR_NEW_ORDERS) continue;

            List<Node> newRoute = this.dispacher.generateRoute(this.portNode);
            // TODO assign route to ship
            dock.cargoToLoad = dock.ship.getCapacity(); // TODO decide whether always fill ships to full capacity
            dock.state = Dock.DockState.LOADING_NEW_CARGO;
        }
    }

    private void releaseDockedShips()
    {
        for(Dock dock: this.docks)
        {
            if(dock.isEmtpy() || dock.state != Dock.DockState.READY_FOR_RELEASE) continue;
            dock.releaseShip();
        }
    }
}
