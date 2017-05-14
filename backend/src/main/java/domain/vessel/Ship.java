package domain.vessel;

import domain.Agent;
import domain.World;
import domain.util.AgentType;
import domain.util.Location;

/**
 * @author Oliver Lea
 */
public class Ship extends Agent {

    public Ship(Location initialLoc) {
        super(AgentType.SHIP, initialLoc);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
