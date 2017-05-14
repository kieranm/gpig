import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.scalecube.socketio.Session;
import io.scalecube.socketio.SocketIOListener;

import java.nio.charset.Charset;

/**
 * Created by liamw on 13/05/2017.
 */
public class MyListener implements SocketIOListener {

    private Session session;

    public MyListener() {}

    @Override
    public void onConnect(Session session) {
        this.session = session;
    }

    @Override
    public void onMessage(Session session, ByteBuf byteBuf) {
    }

    public void sendMessage(String message) {
        if (this.session != null) {
            ByteBuf response = Unpooled.copiedBuffer(message, Charset.defaultCharset());
            session.send(response);
        }
    }

    @Override
    public void onDisconnect(Session session) {
        this.session = null;
    }
}
