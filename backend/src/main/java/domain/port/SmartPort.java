package domain.port;

import domain.Agent;
import domain.World;
import domain.util.AgentType;
import domain.util.Location;

/**
 * @author Oliver Lea
 */
public class SmartPort extends Agent {

    public SmartPort(Location loc) {
        super(AgentType.SMART_PORT, loc);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
