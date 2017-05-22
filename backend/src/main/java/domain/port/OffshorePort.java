package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public class OffshorePort extends Port {

    public OffshorePort(String name, Node node, int capacity) {
        super(AgentType.SMART_PORT, name, node, capacity);
    }

    @Override
    public void tick(World world, int multiplier) {
        this.updatePort(multiplier);
    }
}
