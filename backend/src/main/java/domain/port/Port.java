package domain.port;

import domain.Agent;
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

    public Port(AgentType agentType, String name,
                Node node, int capacity, int load) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
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
    public void setLoad(int load) {
        this.load = load;
    }
}
