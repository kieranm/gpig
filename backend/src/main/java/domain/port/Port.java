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
    private Dispatcher dispacher;

    public Port(AgentType agentType, String name,
                Node node, int capacity, int load, Dispatcher dispacher) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
        this.dispacher = dispacher;
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


    public Ship DispatchShip(Ship ship)
    {
        List<Node> route = this.dispacher.generateRoute(this.portNode);
        //TODO assing route to node
        return ship;
    }
}
