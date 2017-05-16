package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.vessel.Ship;
import domain.world.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Lea
 */
public abstract class Port extends Agent implements Carrier {

    private String name;

    private int capacity;
    private int load;

    private List<Ship> queueingShips;

    private Node node;

    public Port(AgentType agentType, String name, Node node, int capacity, int load) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
        this.queueingShips = new ArrayList<>();
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
}
