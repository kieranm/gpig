package domain.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {

    private List<Node> nodes;
    private Integer weight;
    private boolean isActive = true;

    public Route(List<Node> nodes, Integer weight) {
        this.nodes = nodes;
        this.weight = weight;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Integer getWeight() {
        return weight;
    }

    public void enable() {
        this.isActive = true;
    }

    public void disable() {
        this.isActive = false;
    }

    public boolean isActive() { return isActive; }

    public Route reverse() {
        List<Node> reversedNodes = new ArrayList<>(this.nodes);
        Collections.reverse(reversedNodes);
        return new Route(reversedNodes, this.weight);
    }

    public boolean areEqualRoutes(Route route) {
        List<Node> n1 = route.getNodes();
        List<Node> n2 = this.getNodes();
        if(n1.size() != n2.size()) return false;

        for(int i = 0; i < n1.size(); i++)
            if(!n1.get(i).equals(n2.get(i))) return false;

        return true;
    }
}
