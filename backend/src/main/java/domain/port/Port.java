package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
abstract class Port extends Agent implements Carrier {

    private String name;
    private int capacity;
    private int load;

    public Port(AgentType agentType, String name,
                Coordinates coord, int capacity, int load) {
        super(agentType, coord);
        this.name = name;
        this.capacity = capacity;
        this.load = load;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public int getLoad() {
        return this.load;
    }
}
