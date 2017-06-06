package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * Created by liamw on 27/05/2017.
 */
public class Aircraft extends Ship {

    public Aircraft(Coordinates initialLoc, int capacity) {
        super(AgentType.AIRCRAFT, initialLoc, capacity);
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
