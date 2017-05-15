package domain.world;

import domain.util.Coordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liamw on 14/05/2017.
 */
public class Node {

    private List<Node> neighbors;
    private Coordinates coordinates;
    private Integer nodeID;

    public Node(Integer nodeID, Coordinates coordinates) {
        this.neighbors = new ArrayList<Node>();
        this.coordinates = coordinates;
        this.nodeID = nodeID;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Integer getNodeID() { return nodeID; }
    public void addNeighbor(Node n) { neighbors.add(n); }
    public List<Node> getNeighbors() { return neighbors; }

}
