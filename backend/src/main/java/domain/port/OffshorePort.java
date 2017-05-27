package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.vessel.Ship;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public class OffshorePort extends Port {

    public OffshorePort(String name, Node node, int capacity, int dock_capacity) {
        super(AgentType.SMART_PORT, name, node, capacity, dock_capacity);
        this.setLoad(0);
    }

    @Override
    public void tick(World world, int multiplier) {
        this.updatePort(multiplier);
    }

    @Override
    void produceCargo() {}

    @Override
    void unloadDockedShip(Ship ship, int multiplier) {
        int requestedUnload = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        requestedUnload *= multiplier;
        int amountUnloaded = ship.unloadCargo(requestedUnload);
        amountUnloaded =  Math.min(amountUnloaded, this.cargoCapacity - this.cargoLoad);
        this.stats.addDeliveredCargo(amountUnloaded);
        this.cargoLoad += amountUnloaded;
        if (ship.isEmpty()) {
            this.dockLoad -= ship.getCapacity();
            ship.setState(Ship.ShipState.IDLE);
        } else if (this.isFull()) {
            this.dockLoad -= ship.getCapacity();
            ship.setState(Ship.ShipState.WAITING_LOADING);
            sendToBack(ship);
        }
    }
}
