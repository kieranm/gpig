package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;
import domain.world.ShippingNetwork;

import java.util.List;

/**
 * @author Oliver Lea
 */
public class SmartPort extends Port {

    public SmartPort(String name, Node node, int capacity, int load) {
        super(AgentType.SMART_PORT, name, node, capacity, load);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
