import java.util.Random;

/**
 * Created by liamw on 13/05/2017.
 */
public class Ship extends Agent {

    public Ship(String shipID) {
        this.id = shipID;
        Random r = new Random();
        this.longitude = ((r.nextDouble() - 0.5)*2) * 180.0;
        this.latitude = ((r.nextDouble() - 0.5)*2) * 90.0;
    }

    public Ship(String shipID, double initialX, double initialY) {
        this.id = shipID;
        this.longitude = initialX;
        this.latitude = initialY;
    }
}
