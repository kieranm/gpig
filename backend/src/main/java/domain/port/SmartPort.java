package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;

public class SmartPort extends Port {

    public SmartPort(String name, Node node, int capacity, int load) {
        super(AgentType.LAND_PORT, name, node, capacity, load);
    }

    @Override
    public void tick(World world, int multiplier) {
        this.updateDocks(multiplier);
        // TODO, extend potentially
    }
}
