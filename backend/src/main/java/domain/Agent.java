package domain;

import domain.util.AgentType;
import domain.util.JSONable;
import domain.util.Location;
import org.json.JSONObject;
import utils.IdGenerator;

/**
 * @author Oliver Lea
 */
public abstract class Agent implements JSONable {

    private int id;
    private Location location;
    private AgentType agentType;

    public Agent(AgentType agentType, Location initialLocation) {
        this.id = IdGenerator.getId();
        this.agentType = agentType;
        this.location = initialLocation;
    }

    public abstract void tick(World world);

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("type", this.agentType.toString())
                .put("location", this.location.toJSON());
    }

    public int getId() {
        return id;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
