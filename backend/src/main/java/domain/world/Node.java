package domain.world;

import domain.port.Port;
import domain.util.Coordinates;

public class Node {

    private Coordinates coordinates;
    private Integer nodeID;
    private Port port;

    public Node(int nodeID, Coordinates coordinates) {
        this.coordinates = coordinates;
        this.nodeID = nodeID;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Integer getNodeID() {
        return nodeID;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    /**
     * Nullable port
     */
    public Port getPort() {
        return this.port;
    }
}
