package domain;

import domain.util.JSONable;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * @author Oliver Lea
 */
public class World implements JSONable {

    private Session host;
    private List<Agent> agents;
    private Timer timer;

    public World(Session host, List<Agent> agents) {
        this.host = host;
        this.agents = agents;

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                World.this.tick();
                World.this.sendToRemote();
            }
        };
        this.timer = new Timer();
        this.timer.schedule(tt, 1000l, 200l);
    }

    public void tick() {
        agents.forEach(a -> a.tick(this));
    }

    private void sendToRemote() {
        try {
            host.getRemote().sendString(String.valueOf(this.toJSON()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
