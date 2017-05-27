package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.vessel.Ship;
import domain.world.Node;

/**
 * Created by liamw on 27/05/2017.
 */
public class AidSite extends Port {

    public AidSite(String name, Node node, int capacity, int dock_capacity) {
        super(AgentType.AID_PORT, name, node, capacity, dock_capacity);
    }

    @Override
    public void tick(World world, int multiplier) {

    }

    @Override
    void produceCargo(int multiplier) {

    }

    @Override
    void unloadDockedShip(Ship ship, int multiplier) {

    }
}
