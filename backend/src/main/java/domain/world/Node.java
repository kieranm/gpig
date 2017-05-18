package domain.world;

import domain.util.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private List<Node> neighbors;
    private Coordinates coordinates;
    private Integer nodeID;

    public Node(Integer nodeID, Coordinates coordinates) {
        this.neighbors = new ArrayList<>();
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
