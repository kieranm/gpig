package domain;

import domain.util.AgentType;
import domain.vessel.Ship;
import org.json.JSONArray;
import org.json.JSONObject;
import server.Statistics;
import utils.JSONable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Oliver Lea
 */
public class World implements JSONable {

    private static final int STAT_UPDATE_PERIOD = 1;

    private int multiplier = 1;
    private boolean showWeather = false;

    private Statistics stats;
    private int sinceLastStatSend;

    private List<Agent> agents;

    public World(List<Agent> agents) {
        this.agents = agents;
        List<Ship> ships = new ArrayList<>();
        for (Agent a : agents) {
            if (a.getAgentType() == AgentType.SMART_SHIP || a.getAgentType() == AgentType.FREIGHT_SHIP) {
                ships.add((Ship) a);
            }
        }
        this.stats = new Statistics(ships);
        this.sinceLastStatSend = 0;
        this.agents.forEach(a -> a.setStats(stats));
    }

    public void tick() {
        agents.forEach(a -> a.tick(this, multiplier));
        agents = agents.stream().collect(toList());
    }

    @Override
    public JSONObject toJSON() {
        Map<String, Object> m = new HashMap<>(2);
        m.put("agents", new JSONArray(
                this.agents.stream().filter(Agent::isAlive)
                        .map(Agent::toJSON).collect(toList())
        ));
        if (sinceLastStatSend == STAT_UPDATE_PERIOD) {
            m.put("statistics", stats.toJSON());
            sinceLastStatSend = 0;
        } else {
            sinceLastStatSend++;
        }
        return new JSONObject(m);
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setShowWeather(boolean value) {
        this.showWeather = value;
    }

    public boolean getShowWeather(){
        return this.showWeather;
    }

    public Statistics getStats() {
        return stats;
    }
}
