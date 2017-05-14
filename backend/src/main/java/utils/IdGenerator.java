package utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Oliver Lea
 */
public class IdGenerator {

    private final static AtomicInteger counter = new AtomicInteger();

    public static int getId() {
        return counter.incrementAndGet();
    }
}
