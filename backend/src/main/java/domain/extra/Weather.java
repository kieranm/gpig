package domain.extra;

import domain.Agent;
import domain.World;
import domain.port.Port;
import domain.util.AgentType;
import domain.util.Coordinates;
import domain.vessel.Ship;
import domain.world.Route;

import java.util.*;

public class Weather extends Agent {

    private final int TIMEOUT = 10000; // In ticks

    private double range;

    private List<Port> affectedPorts = new ArrayList<>();
    private List<Route> orgRoutes = new ArrayList<>();
    private List<Route> altRoutes = new ArrayList<>();
    private int tickCount = 0;

    public Weather(Coordinates initialCoordinates, double range) {
        super(AgentType.WEATHER, initialCoordinates);
        super.kill();
        this.range = range;
    }

    @Override
    public void tick(World world, int multiplier) {
        // SPAWN WEATTHE
        this.rewive();

        if(this.isAlive()) {
            this.tickCount += 1 * multiplier;
            if(this.tickCount > TIMEOUT) {

                this.kill();
                this.setNewPathsForAffectedPorts(this.altRoutes, this.orgRoutes); // reset paths
                return;
            }

            checkForShipsInRange();
        }
    }

    private void addAffectedPort(Port port) { this.affectedPorts.add(port); }
    private void addRoutes(Route orgRoute, Route altRoute) {
        this.orgRoutes.add(orgRoute);
        this.altRoutes.add(altRoute);

        this.orgRoutes.add(orgRoute.reverse());
        this.altRoutes.add(altRoute.reverse());
    }

    private void checkForShipsInRange() {
        for(Port port : this.affectedPorts)
            for(Ship s : port.getManagedShips()){
                if(s.getAgentType() != AgentType.SMART_SHIP && s.getState() == Ship.ShipState.TRAVELING &&
                    s.getCoordinates().distance(this.getCoordinates()) <= range) continue;

                this.setNewPathsForAffectedPorts(this.orgRoutes, this.altRoutes);
                return;
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

                port.setNewRoute(key, value);
            }
        }
    }
}
