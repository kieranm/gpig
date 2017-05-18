package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.world.Node;
import domain.world.Route;

import java.util.List;
import java.util.Map;


public class SmartPort extends Port {

    public SmartPort(String name, Node node,  Map<Port, List<Route>> routes, Map<Port, Double> probabilities,
                    int capacity, int load, int cargoMoveSpeed) {
        super(AgentType.LAND_PORT, name, node, routes, probabilities, capacity, load, cargoMoveSpeed);
    }

    @Override
    public void tick(World world) {
        int simulationSpeed = 1; // TODO decide how we process this value

        this.updateDocks(simulationSpeed);
        // TODO, extend potentially
    }
}
