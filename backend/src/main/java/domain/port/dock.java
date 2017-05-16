package domain.port;

import domain.vessel.Ship;

public class Dock {

    // might need to replace with enum if more advanced operations will be performed
    public DockState state;
    public Ship ship;
    public int cargoToLoad = 0;

    public enum DockState
    {
        UNLOADING_OLD_CARGO,
        READY_FOR_NEW_ORDERS,
        LOADING_NEW_CARGO,
        READY_FOR_RELEASE
    }

    public boolean isEmtpy(){return ship == null;}
    public void releaseShip() {this.ship = null;}

    public void moorShip(Ship ship)
    {
        this.state = DockState.UNLOADING_OLD_CARGO;
        this.ship = ship;
    }
}
