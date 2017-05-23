package server;

import domain.vessel.Ship;
import org.json.JSONObject;
import utils.JSONable;

import java.util.*;

/**
 * Created by Oliver Lea.
 */
public class Statistics implements JSONable {

    private int totalCargoDelivered;
    private int throughput;
    private double averageWaitingTime;

    private Collection<Ship> ships;

    public Statistics(Collection<Ship> ships) {
        this.ships = ships;
    }

    public void nextTick() {
        this.averageWaitingTime = 0;
        this.throughput = 0;
    }

    public int getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public void addDeliveredCargo(int cargo) {
        this.totalCargoDelivered += cargo;
        this.throughput += cargo;
    }

    public int getThroughput() {
        return throughput;
    }

    public double getAverageWaitingTime() {
        int total = this.ships.stream()
                .map(Ship::getWaitingTime)
                .filter(Objects::nonNull)
                .mapToInt(d -> d)
                .sum();
        return (double) total / (double) this.ships.size();
    }

    @Override
    public JSONObject toJSON() {
        Map<String, Number> m = new HashMap<>();
        m.put("total_cargo_delivered", getTotalCargoDelivered());
        m.put("total_throughput", getThroughput());
        m.put("average_waiting_time", getAverageWaitingTime());
        return new JSONObject(m);
    }
}
