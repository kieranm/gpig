package domain.port;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.world.Node;
import domain.world.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Lea
 */
public abstract class Port extends Agent implements Carrier {

    private String name;
    private int capacity;
    private int load;
    private Node node;
    private Map<Port, List<Route>> routes = new HashMap<>();
    private Map<Port, Double> probabilities = new HashMap<>();

    public Port(AgentType agentType, String name, Node node, int capacity, int load) {
        super(agentType, node.getCoordinates());
        this.node = node;
        this.name = name;
        this.capacity = capacity;
        this.load = load;
    }

    //TODO is this needed?
    public Map<Port, List<Route>> getRoutes() {
        return routes;
    }

    public Node getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public int getLoad() {
        return this.load;
    }

    public Route generateRoute() {
        //TODO random selection of possible destination
        for (Port p : this.routes.keySet()) {
            return this.routes.get(p).get(0);
        }
        return null;
    }

    public void addRoute(Port destination, Route route) {
        if (this.routes.get(destination) == null) {
            List<Route> newRoutes = new ArrayList<>();
            newRoutes.add(route);
            this.routes.put(destination, newRoutes);
        } else {
            this.routes.get(destination).add(route);
        }

        updateProbabilities();
    }

    private void updateProbabilities() {
        //TODO
    }
}
