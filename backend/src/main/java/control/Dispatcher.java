package control;

import domain.world.Node;
import java.util.*;

public class Dispatcher {

    // All nodes in the world. Used for route Generation. Might need to change into presset routes in future
    List<Node> allNodes = new LinkedList<>();

    public Dispatcher()
    {
        //TODO extend
    }

    public void addNode(Node node){ allNodes.add(node);}

    public List<Node> generateRoute(Node portNode)
    {
        List<Node> newRoute = new LinkedList<>();
        //TODO: generate Route
        return newRoute;
    }
}
