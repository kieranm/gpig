package domain.vessel;

import domain.Agent;
import domain.World;
import domain.util.AgentType;
import domain.util.Location;

/**
 * @author Oliver Lea
 */
public class SmartBoat extends Agent {

    public SmartBoat(Location initialLoc) {
        super(AgentType.SMART_BOAT, initialLoc);
    }

    @Override
    public void tick(World world) {
        // TODO
    }
}
