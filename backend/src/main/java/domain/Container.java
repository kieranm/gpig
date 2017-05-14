package domain;

import utils.IdGenerator;

/**
 * @author Oliver Lea
 */
public class Container {

    private int id;

    private int destId;
    private int hostId;

    public Container(int destId, int hostId) {
        this.id = IdGenerator.getId();
        this.destId = destId;
        this.hostId = hostId;
    }

    public int getDestId() {
        return destId;
    }

    public int getHostId() {
        return hostId;
    }
}
