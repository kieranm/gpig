package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
public class SmartShip extends Ship {

    // TODO use different sizes?
    public static final int SMALL_CAPACITY = 100;
    public static final int MEDIUM_CAPACITY = 500;
    public static final int LARGE_CAPACITY = 1000;


    public SmartShip(Coordinates initialCoord, int capacity) {
        super(AgentType.SMART_SHIP, initialCoord, capacity);
    }

    @Override
    public void tick(World world, int multiplier) {
        switch(this.getState()) {
            case WAITING_LOADING:
                this.addWaitingTime(multiplier);
                break;
            case WAITING_UNLOADING:
                this.addWaitingTime(multiplier);
                break;
            case TRAVELING:
                this.followRoute(multiplier);
                break;
        }
    }
}
