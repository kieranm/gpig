package domain;

import domain.world.ShippingNetwork;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.JSONable;

import java.util.List;

import static java.util.stream.Collectors.toList;

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
        agents = agents.stream().filter(Agent::isAlive).collect(toList());
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("agents", new JSONArray(
                this.agents.stream().map(Agent::toJSON).collect(toList())
        ));
        System.out.println(obj);
        return obj;
    }

    public List<Agent> getAgents() {
        return agents;
    }
}
