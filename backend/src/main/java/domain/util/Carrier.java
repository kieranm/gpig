package domain.util;

/**
 * @author Oliver Lea
 */
public interface Carrier {

    int getCapacity();
    int getLoad();
    int loadContainers(int count);
    int unloadContainers(int count);
    boolean isEmpty();
    boolean isFull();
}
