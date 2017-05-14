import server.Server;

/**
 * @author Oliver Lea
 */
public class App {

    public static void main(String[] args) {
        new Server(4810).run();
    }
}
