import io.netty.buffer.ByteBuf;

/**
 * Created by liamw on 13/05/2017.
 */
public abstract class Agent {

    double longitude;
    double latitude;

    String id;

    //{"some-random-ship-id": {x: -2.69165, y: 49.91741}}
    public String getSimulationData() {
        return "{ \""+id+"\" {x: "+Double.toString(longitude)+", y: "+Double.toString(latitude)+"}}";
    }

}
