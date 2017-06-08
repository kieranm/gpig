package domain.vessel;

import domain.World;
import domain.util.AgentType;
import domain.util.Coordinates;
import domain.world.Node;
import utils.IdGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by liamw on 08/06/2017.
 */
public class AidShip extends Ship {

    int counter;
    int delay;
    List<Node> nodeList;

    public AidShip(Coordinates initialLoc, int capacity) {
        super(AgentType.SMART_SHIP, initialLoc, capacity);

        nodeList = new ArrayList<>();
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(26.532482, -83.298281)));
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(23.82212, -82.74697)));
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(22.94117, -78.69682)));
        nodeList.add(new Node(IdGenerator.getId(), new Coordinates(20.36487, -73.74395)));

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
                this.assignRoute(nodeList);
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
