package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Oliver Lea
 */
@WebSocket
public class SimulationHandler {

    private static Map<Session, Simulation> sessions = new ConcurrentHashMap<>();


    @OnWebSocketConnect
    public void onConnect(Session sess) throws Exception {
        System.out.println("Connection established");
        sessions.put(sess, new Simulation(sess));
    }

    @OnWebSocketClose
    public void onClose(Session sess, int statusCode, String reason) throws Exception {
        System.out.println("Connection closed");
        sessions.get(sess).end();
        sessions.remove(sess);
    }

    @OnWebSocketMessage
    public void onMessage(Session sess, String message) throws Exception {
        JSONObject json = new JSONObject(message);
        if (json.getString("message_type").equals("change_weather")) {
             sessions.get(sess).setShowWeather(json.getBoolean("message_data"));
        }
        if (json.getString("message_type").equals("change_speed")) {
            sessions.get(sess).setMultiplier(json.getInt("message_data"));
        }
        if (json.getString("message_type").equals("change_mode")) {
            sessions.get(sess).switchWorld(json.getString("message_data"));
        }
        if (json.getString("message_type").equals("start")) {
            Simulation simulation = sessions.get(sess);

            JSONObject message_data = json.getJSONObject("message_data");

            simulation.setMultiplier(message_data.getInt("speed_multiplier"));
            //message_data.getString("mode"); legacy or oceanx

            simulation.start();
        }
    }
}
