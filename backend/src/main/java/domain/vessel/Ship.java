package domain.vessel;

import domain.Agent;
import domain.util.AgentType;
import domain.util.Carrier;
import domain.util.Coordinates;
import domain.world.Node;

import java.util.Collections;
import java.util.List;

/**
 * @author Oliver Lea
 */
public abstract class Ship extends Agent implements Carrier {

    private int capacity;
    private int load;
    private List<Node> route;
    private Node next;

    public Ship(AgentType agentType, Coordinates initialLoc, int capacity, int load) {
        super(agentType, initialLoc);
        this.capacity = capacity;
        this.load = load;
    }

    public void setRoute(List<Node> route) {
        this.route = route;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void startReturnTrip() {
        Collections.reverse(route);
        this.next = route.get(0);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getLoad() {
        return load;
    }

    /**
     * @return whether the end of the route has been reached
     */
    public boolean nextRouteStop() {
        int current = this.route.indexOf(this.next);
        if (current == this.route.size()-1) {
            return true;
        } else {
            this.next = this.route.get(current+1);
            return false;
        }
    }
}
