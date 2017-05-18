package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.vessel.Ship;
import domain.world.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class Port extends Agent implements Carrier {

    private static final double CAPACITY_PORT_SIZE_RATIO = 0.01;
    private String name;

    private int capacity;
    private int load;

    private Node node;

    private int dockLoad = 0;
    private List<Ship> dockedShips;
    private List<Ship> waitingShips = new ArrayList<>();

    public Port(AgentType agentType, String name, Node node, int capacity, int load) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
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
}
