package server;

import domain.vessel.Ship;
import org.json.JSONObject;
import utils.JSONable;

import java.util.*;

/**
 * Created by Oliver Lea.
 */
public class Statistics implements JSONable {

    private static final int THROUGHPUT_AVERAGE_SIZE = 20;

    private int[] throughputAverage;
    private int throughputIndex;

    private int totalCargoDelivered;
    private int throughput;

    private Collection<Ship> ships;

    public Statistics(Collection<Ship> ships) {
        this.ships = ships;
        throughputAverage = new int[THROUGHPUT_AVERAGE_SIZE];
        this.throughputIndex = 0;
    }

    public void nextTick() {
        this.throughputAverage[this.throughputIndex] = this.throughput;
        this.throughputIndex++;
        if (this.throughputIndex == THROUGHPUT_AVERAGE_SIZE) {
            this.throughputIndex = 0;
        }
        this.throughput = 0;
    }

    public int getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public void addDeliveredCargo(int cargo) {
        this.totalCargoDelivered += cargo;
        this.throughput += cargo;
    }

    public double getThroughput() {
        return Arrays.stream(this.throughputAverage).average().getAsDouble();
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
