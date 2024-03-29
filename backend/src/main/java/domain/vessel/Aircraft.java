package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;
import domain.world.Node;
import utils.IdGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by liamw on 27/05/2017.
 */
public class Aircraft extends Ship {

    List<List<Node>> routes;
    private List<Node> nodeList;
    Node start;
    int delay;
    int counter;

    public Aircraft(Coordinates initialLoc, int capacity) {
        super(AgentType.AIRCRAFT, initialLoc, capacity);

        routes = new ArrayList<>();

        nodeList = new ArrayList<>();
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(20.36487, -73.74395)));
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(18.59935, -72.2368)));
        routes.add(nodeList);

        nodeList = new ArrayList<>();
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(20.36487, -73.74395)));
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(19.96937, -75.121)));
        routes.add(nodeList);

        nodeList = new ArrayList<>();
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(20.36487, -73.74395)));
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(18.46162, -74.18643)));
        routes.add(nodeList);

        this.start = nodeList.get(0);
        this.setCoordinates(new Coordinates(nodeList.get(0).getCoordinates().getLatitude(),
                nodeList.get(0).getCoordinates().getLongitude()));
        this.assignRoute(nodeList);
        this.setLoad(this.getCapacity());

        delay = ((int) (Math.random() * 100));
        counter = 0;
    }

    @Override
    public void tick(World world, int multiplier) {
        if (delay > counter) {
            counter++;
            return;
        }

        // check if we have reached the next waypoint
        // TODO figure out what a sensible distance is to be considered "on" the next waypoint
        if (hasReachedPoint()) {

            // if the end of the route has been reached attempt to dock
            // else set the next route point
            if (routeEndReached()) {
                if (this.isFull()) {
                    this.setLoad(0);
                } else {
                    this.setLoad(this.getCapacity());
                }
                Collections.reverse(nodeList);
                if (this.getCoordinates().equals(start.getCoordinates())) {
                    Random rand = new Random();
                    int val = rand.nextInt(3);
                    this.assignRoute(routes.get(val));
                } else {
                    this.assignRoute(nodeList);
                }
                return;
            } else {
                nextRouteStop();
            }
            // calculate new vector toward next waypoint
            calculatePositionUpdateVector();
        }

        // move toward next
        this.setCoordinates(this.getCoordinates().add(this.positionUpdateVector.mul((double) multiplier)));
        if (this.getCoordinates().distance(this.next.getCoordinates()) < this.positionUpdateVector.mul((double) multiplier).length()) {
            this.setCoordinates(new Coordinates(this.next.getCoordinates()));
        }
    }
}
