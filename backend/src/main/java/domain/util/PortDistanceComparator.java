package domain.util;

import domain.port.Port;

import java.util.Comparator;

/**
 * Created by liamw on 19/05/2017.
 */
public class PortDistanceComparator implements Comparator<Port> {

    private Port myPort;

    public PortDistanceComparator(Port myPort) {
        this.myPort = myPort;
    }

    // -1 less than, 0 equal to, 1 greater than
    @Override
    public int compare(Port o1, Port o2) {
        Double o2distance = o2.getCoordinates().distance(myPort.getCoordinates());
        Double o1distance = o1.getCoordinates().distance(myPort.getCoordinates());
        return Double.compare(o1distance, o2distance);
    }
}
