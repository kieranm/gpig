package domain.port;

import domain.World;
import domain.util.AgentType;
import domain.vessel.Ship;
import domain.world.Node;

/**
 * @author Oliver Lea
 */
public class CoastalPort extends Port {

    public CoastalPort(String name, Node node, int capacity, int dock_capacity) {
        super(AgentType.LAND_PORT, name, node, capacity, dock_capacity);
    }

    void produceCargo() {
        int newCargo = ((int) (CAPACITY_CARGO_PRODUCTION_RATIO * ((double) this.cargoCapacity)));
        newCargo = Math.min(newCargo, this.cargoCapacity - this.cargoLoad);
        this.cargoLoad += newCargo;
    }

    void unloadDockedShip(Ship ship, int multiplier){
        int requestedUnload = BASE_LOAD_UNLOAD_SPEED * ((ship.getCapacity() / SHIP_SIZE_LOADING_OFFSET) + 1);
        requestedUnload *= multiplier;
        int amountUnloaded = ship.unloadCargo(requestedUnload);
        amountUnloaded =  Math.min(amountUnloaded, this.cargoCapacity - this.cargoLoad);
        this.stats.addDeliveredCargo(amountUnloaded);
        if (ship.isEmpty()) {
            this.dockLoad -= ship.getCapacity();
            ship.setState(Ship.ShipState.IDLE);
        } else if (this.isFull()) {
            this.dockLoad -= ship.getCapacity();
            ship.setState(Ship.ShipState.WAITING_LOADING);
            sendToBack(ship);
        }
    }

    @Override
    public void tick(World world, int multiplier) {
        this.updatePort(multiplier);
    }
}
