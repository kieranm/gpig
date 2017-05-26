package domain;

import domain.util.AgentType;
import domain.util.Coordinates;
import org.json.JSONObject;
import server.Statistics;
import utils.IdGenerator;
import utils.JSONable;

/**
 * @author Oliver Lea
 */
public abstract class Agent implements JSONable {

    private int id;
    private boolean alive;
    private Coordinates coordinates;
    private AgentType agentType;

    protected Statistics stats;

    public Agent(AgentType agentType, Coordinates initialCoordinates) {
        this.id = IdGenerator.getId();
        this.agentType = agentType;
        this.coordinates = initialCoordinates;
        this.alive = true;
    }

    public abstract void tick(World world, int multiplier);

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("type", this.agentType.toString())
                .put("coordinates", this.coordinates.toJSON());
    }

    public int getId() {
        return id;
    }

    public boolean isAlive() {
        return alive;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setStats(Statistics stats) {
        this.stats = stats;
    }

    @Override
    public int hashCode() {
        return this.getId();
    }
}
