package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Oliver Lea
 */
public class SmartShip extends Ship {

    private List<Coordinates> path = Arrays.asList(
            new Coordinates(-2.69165,49.91741),
            new Coordinates(-3.79028,49.69051),
            new Coordinates(-4.91089,49.46255),
            new Coordinates(-5.76782,49.20481),
            new Coordinates(-6.427,49.00342),
            new Coordinates(-7.83325,48.81568),
            new Coordinates(-8.86597,48.7143),
            new Coordinates(-9.52515,48.32133),
            new Coordinates(-9.74487,47.92532),
            new Coordinates(-9.7229,47.42228),
            new Coordinates(-10.95337,46.46222)
    );
    private int ind = 0;

    public SmartShip(Coordinates initialCoord, int capacity, int load) {
        super(AgentType.SMART_SHIP, initialCoord, capacity, load);
    }

    @Override
    public void tick(World world) {
        if (ind >= path.size()) {
            Collections.reverse(path);
            this.ind = 0;
        }
        this.setCoordinates(this.path.get(this.ind++));
    }
}
