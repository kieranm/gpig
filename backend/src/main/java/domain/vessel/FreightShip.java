package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
public class FreightShip extends Ship {

    public static final int SMALL_CAPACITY = 1000;
    public static final int MEDIUM_CAPACITY = 2500;
    public static final int LARGE_CAPACITY = 5000;

    public FreightShip(Coordinates initialCoord, int capacity) {
        super(AgentType.FREIGHT_SHIP, initialCoord, capacity);
    }

    @Override
    public void tick(World world, int multiplier) {
        switch(this.getState()) {
            case WAITING_UNLOADING:
                this.addWaitingTime(multiplier);
                break;
            case WAITING_LOADING:
                this.addWaitingTime(multiplier);
                break;
            case TRAVELING:
                this.followRoute(multiplier);
                break;
        }
    }

}
