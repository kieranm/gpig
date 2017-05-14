package domain;

import domain.util.JSONable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oliver Lea
 */
public class World implements JSONable {


    private List<Agent> agents;

    public World(List<Agent> agents) {
        this.agents = agents;
    }

    public void tick() {
        agents.forEach(a -> a.tick(this));
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("agents", new JSONArray(
                this.agents.stream().map(Agent::toJSON).collect(Collectors.toList())
        ));
        return obj;
    }

    public List<Agent> getAgents() {
        return agents;
    }
}
