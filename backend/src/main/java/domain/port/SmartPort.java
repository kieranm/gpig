package domain.port;

import control.Dispatcher;
import domain.World;
import domain.util.AgentType;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public class SmartPort extends Port {

    public SmartPort(String name, Node node, int capacity, int load,  Dispatcher dispacher) {
        super(AgentType.SMART_PORT, name, node, capacity, load, dispacher);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
