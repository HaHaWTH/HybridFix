package network.ycc.raknet.packet;

import io.netty.buffer.ByteBuf;

public class Disconnect extends SimpleFramedPacket {

    public Disconnect() {
        reliability = Reliability.RELIABLE_ORDERED;
        orderChannel = 7;
    }

    @Override
    public void encode(ByteBuf buf) {
        // NOOP
    }

    @Override
    public void decode(ByteBuf buf) {
        // NOOP
    }

}
