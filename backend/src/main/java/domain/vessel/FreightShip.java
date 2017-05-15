package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
public class FreightShip extends Ship {

    public FreightShip(Coordinates initialCoord, int capacity, int load) {
        super(AgentType.FREIGHT_SHIP, initialCoord, capacity, load);
    }

    @Override
    public void tick(World world) {
        // TODO
    }

}
