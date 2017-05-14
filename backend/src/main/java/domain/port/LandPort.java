package domain.port;

import domain.Agent;
import domain.World;
import domain.util.AgentType;
import domain.util.Location;

/**
 * @author Oliver Lea
 */
public class LandPort extends Agent {

    public LandPort(Location loc) {
        super(AgentType.LAND_PORT, loc);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
