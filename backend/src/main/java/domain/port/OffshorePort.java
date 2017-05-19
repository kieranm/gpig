package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public class OffshorePort extends Port {

    public OffshorePort(String name, Node node, int capacity, int load) {
        super(AgentType.SMART_PORT, name, node, capacity, load);
    }

    @Override
    public void tick(World world) {
        this.updatePort(multiplier);
    }
}
