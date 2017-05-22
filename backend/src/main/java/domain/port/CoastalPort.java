package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public class CoastalPort extends Port {

    public CoastalPort(String name, Node node, int capacity) {
        super(AgentType.LAND_PORT, name, node, capacity);
    }

    @Override
    public void tick(World world, int multiplier) {
        this.updatePort(multiplier);
    }
}
