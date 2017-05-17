package domain.world;

/**
 * Created by liamw on 14/05/2017.
 */
public class Edge {

    private double distance;
    private Node node1;
    private Node node2;

    public Edge(Node start, Node end) {
        this.distance = start.getCoordinates().distance(end.getCoordinates());
        this.node1 = start;
        this.node2 = end;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
