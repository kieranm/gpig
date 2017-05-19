package domain.extra;

import domain.Agent;
import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

/**
 * @author Oliver Lea
 */
public class Weather extends Agent {

    public Weather(Coordinates initialCoordinates) {
        super(AgentType.WEATHER, initialCoordinates);
    }

    @Override
    public void tick(World world, int multiplier) {
        // TODO
    }
}
