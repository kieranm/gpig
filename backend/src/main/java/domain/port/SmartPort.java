package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;
import domain.world.ShippingNetwork;

import java.util.List;

/**
 * @author Oliver Lea
 */
public class SmartPort extends Port {

    public SmartPort(String name,  Node node, List<Node> destinations, ShippingNetwork sn,
                     int capacity, int load, int portSize, int cargoMoveSpeed) {
        super(AgentType.SMART_PORT, name, node, destinations, sn, capacity, load, portSize, cargoMoveSpeed);
    }

    @Override
    public void tick(World world) {
        int simulationSpeed = 1; // TODO decide how we process this value

        this.updateDocks(simulationSpeed);
        // TODO, extend potentially
    }
}
