package domain;

import domain.world.ShippingNetwork;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.JSONable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oliver Lea
 */
public class World implements JSONable {


    private List<Agent> agents;
    private ShippingNetwork shippingNetwork;

    public World(List<Agent> agents, ShippingNetwork sn) {
        this.agents = agents;
        this.shippingNetwork = sn;
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
