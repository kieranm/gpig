package domain.world;

import domain.vessel.Ship;
import server.Simulation;

import java.util.*;

/**
 * Created by liamw on 14/05/2017.
 */
public class ShippingNetwork {

    private Map<Integer, Node> nodes;
    private Map<String, Edge> edges;

    public Map<Integer, Node> getNodes() { return nodes; }
    public Map<String, Edge> getEdges() { return edges; }

    public ShippingNetwork(Map<Integer, Node> nodes, Map<String, Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public class NoRouteFoundException extends Exception {}

    public void calculateRoute(Ship ship, Node start, Node end) throws NoRouteFoundException {
        ArrayList<Node> route = astar(start, end);
        if (route == null) { throw new NoRouteFoundException(); }

        ship.setRoute(route);
    }

    private ArrayList<Node> astar(Node start, Node end) {
        List<Node> closedSet = new ArrayList<>();
        List<Node> openSet = new ArrayList<>();
        openSet.add(start);

        Map<Node, Node> cameFrom = new HashMap<>();

        Map<Node, Double> gScore = new HashMap<>();
        Map<Node, Double> fScore = new HashMap<>();

        for (Node n : nodes.values()){
            gScore.put(n, Double.MAX_VALUE);
            fScore.put(n, Double.MAX_VALUE);
        }
        gScore.put(start, 0.0);
        fScore.put(start, start.getCoordinates().distance(end.getCoordinates()));

        while (!openSet.isEmpty()) {
            Node current = openSet.get(0);
            for (Node n : openSet) {
                if (fScore.get(n) < fScore.get(current)) {
                    current = n;
                }
            }

            if (current.equals(end)) {
                ArrayList<Node> route = new ArrayList<>();
                route.add(end);
                while (cameFrom.keySet().contains(current)) {
                    current = cameFrom.get(current);
                    route.add(current);
                }
                Collections.reverse(route);
                return route;
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Node neighbor : current.getNeighbors()) {
                if (closedSet.contains(neighbor)) { continue; }

                double tempgScore = gScore.get(current) + getEdgeDistance(current, neighbor);

                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tempgScore >= gScore.get(neighbor)) {
                    continue;
                }

                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tempgScore);
                fScore.put(neighbor, gScore.get(neighbor) + getEdgeDistance(neighbor, end));
            }
        }

        return null;
    }

    /**
     * Returns the distance stored in the edge variable (may change due to environmental changes)
     * if edge doesn't exist then absolute distance is returned
     *
     * @param n1
     * @param n2
     * @return Distance stored in Edge, or absolute distance
     */
    private double getEdgeDistance(Node n1, Node n2) {
        String s1 = Integer.toString(n1.getNodeID());
        String s2 = Integer.toString(n2.getNodeID());
        Edge edge = edges.get(s1+Simulation.EDGE_MAP_SEPERATOR+s2);
        if (edge != null) {
            return edge.getDistance();
        } else {
            return n1.getCoordinates().distance(n2.getCoordinates());
        }
    }
}
