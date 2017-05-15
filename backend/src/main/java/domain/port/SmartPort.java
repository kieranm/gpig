package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
public class SmartPort extends Port {

    public SmartPort(Coordinates coord, String name, int capacity, int load) {
        super(AgentType.SMART_PORT, name, coord, capacity, load);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
