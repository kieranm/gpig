package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;

public class SmartPort extends Port {

    public SmartPort(String name, Node node, int capacity, int load) {
        super(AgentType.LAND_PORT, name, node, capacity, load);
    }

    @Override
    public void tick(World world) {
        int simulationSpeed = 1; // TODO decide how we process this value

        this.updateDocks(simulationSpeed);
        // TODO, extend potentially
    }
}
