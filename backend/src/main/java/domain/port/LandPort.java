package domain.port;

import control.Dispatcher;
import domain.World;
import domain.util.AgentType;
import domain.world.Node;
/**
 * @author Oliver Lea
 */
public class LandPort extends Port {

    public LandPort(String name, Node node, int capacity, int load, Dispatcher dispatcher) {
        super(AgentType.LAND_PORT, name, node, capacity, load, dispatcher);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
