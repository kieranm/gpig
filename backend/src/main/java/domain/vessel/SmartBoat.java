package domain.vessel;

import domain.Agent;
import domain.World;
import domain.util.AgentType;
import domain.util.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Oliver Lea
 */
public class SmartBoat extends Agent {

    private List<Location> path = Arrays.asList(
            new Location(-2.69165,49.91741),
            new Location(-3.79028,49.69051),
            new Location(-4.91089,49.46255),
            new Location(-5.76782,49.20481),
            new Location(-6.427,49.00342),
            new Location(-7.83325,48.81568),
            new Location(-8.86597,48.7143),
            new Location(-9.52515,48.32133),
            new Location(-9.74487,47.92532),
            new Location(-9.7229,47.42228),
            new Location(-10.95337,46.46222)
    );
    private int ind = 0;

    public SmartBoat(Location initialLoc) {
        super(AgentType.SMART_BOAT, initialLoc);
    }

    @Override
    public void tick(World world) {
        if (ind >= path.size()) {
            Collections.reverse(path);
            this.ind = 0;
        }
        this.setLocation(this.path.get(this.ind++));
    }
}
