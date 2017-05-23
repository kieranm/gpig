package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
public class FreightShip extends Ship {

    // TODO use different sizes?
    public static final int SMALL_CAPACITY = 100;
    public static final int MEDIUM_CAPACITY = 500;
    public static final int LARGE_CAPACITY = 1000;

    public FreightShip(Coordinates initialCoord, int capacity) {
        super(AgentType.FREIGHT_SHIP, initialCoord, capacity);
    }

    @Override
    public void tick(World world, int multiplier) {
        if (this.getState() == ShipState.TRAVELING) {
            this.followRoute(multiplier);
        }
    }

}
