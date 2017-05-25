package domain.extra;

import domain.Agent;
import domain.World;
import domain.port.Port;
import domain.util.AgentType;
import domain.util.Coordinates;
import domain.vessel.Ship;
import domain.world.Route;
import org.json.JSONObject;

import java.util.*;

public class Weather extends Agent {
    private double range;

    private List<Port> affectedPorts = new ArrayList<>();
    private List<Route> orgRoutes = new ArrayList<>();
    private List<Route> altRoutes = new ArrayList<>();

    public Weather(Coordinates initialCoordinates, int range) {
        super(AgentType.WEATHER, initialCoordinates);
        super.kill();
        this.range = range;
    }

    @Override
    public void tick(World world, int multiplier) {
        if(this.isAlive()) {
            if(!world.getShowWeather()){
                this.kill();
                this.setNewPathsForAffectedPorts(this.altRoutes, this.orgRoutes); // reset paths
                this.setNewPathsForAffectedBoats(this.altRoutes, this.orgRoutes); // reset paths
                return;
            }

            checkForShipsInRange();

        }else if(world.getShowWeather()) this.rewive();
    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON().put("range", this.range);
    }

    private void addAffectedPort(Port port) { this.affectedPorts.add(port); }
    private void addRoutes(Route orgRoute, Route altRoute) {
        this.orgRoutes.add(orgRoute);
        this.altRoutes.add(altRoute);

        this.orgRoutes.add(orgRoute.reverse());
        this.altRoutes.add(altRoute.reverse());
    }

    private void checkForShipsInRange() { // updates paths when at least one ship is detected
        for(Port port : this.affectedPorts)
            for(Ship s : port.getManagedShips()){
                if(s.getAgentType() != AgentType.SMART_SHIP && s.getState() == Ship.ShipState.TRAVELING &&
                    s.getCoordinates().distance(this.getCoordinates()) <= range) continue;

                // weather has been detected
                this.setNewPathsForAffectedPorts(this.orgRoutes, this.altRoutes);
                this.setNewPathsForAffectedBoats(this.orgRoutes, this.altRoutes);
                return;
            }
    }

    private void setNewPathsForAffectedBoats(List<Route> orgRoutes, List<Route> altRoutes){
        for(Port port : this.affectedPorts) {
            for(Ship s : port.getManagedShips()) {
                if(s.getState() != Ship.ShipState.TRAVELING) continue;

                for(int i = 0; i < orgRoutes.size(); i++)
                    if(s.getCurrentRoute().equals(orgRoutes.get(i).getNodes()))
                        s.assignRoute(altRoutes.get(i).getNodes());
            }
        }
    }

    private void setNewPathsForAffectedPorts(List<Route> orgRoutes, List<Route> altRoutes){
        for(Port port : this.affectedPorts){
            Map<Port, List<Route>> map = port.getRoutes();

            for (Map.Entry<Port, List<Route>> entry : map.entrySet()) {
                Port key = entry.getKey();
                List<Route> value = entry.getValue();

                for(int i = 0; i < value.size(); i++)
                    for(int j = 0; j < orgRoutes.size(); j++)
                        if(value.get(i).equals(orgRoutes.get(j)))
                            value.set(i, altRoutes.get(j));

                map.put(key, value); // overwrite old route
            }
        }
    }
}
