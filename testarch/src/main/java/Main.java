/**
 * Created by liamw on 13/05/2017.
 */

import io.netty.buffer.Unpooled;
import io.scalecube.socketio.Session;
import io.scalecube.socketio.SocketIOAdapter;
import io.scalecube.socketio.SocketIOServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;

import java.nio.charset.Charset;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        SocketIOServer echoServer = SocketIOServer.newInstance();
        MyListener listener = new MyListener();
        echoServer.setListener(listener);
        echoServer.start();

        Ship a = new Ship("a");
        Ship b = new Ship("b");
        listener.sendMessage(a.getSimulationData());
        listener.sendMessage(b.getSimulationData());
        Random r = new Random();
        while (true) {
            double val = r.nextDouble();
            if (val > 0.7) {
                listener.sendMessage(a.getSimulationData());
                a = new Ship("a");
            }
            val = r.nextDouble();
            if (val > 0.6) {
                listener.sendMessage(b.getSimulationData());
                b = new Ship("b");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
