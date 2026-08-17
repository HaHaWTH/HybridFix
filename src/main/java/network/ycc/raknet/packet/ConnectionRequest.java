package network.ycc.raknet.packet;

import io.netty.buffer.ByteBuf;

public class ConnectionRequest extends SimpleFramedPacket implements Packet.ClientIdConnection {

    private static final int HYBRIDFIX_PROTOCOL_EXTENSION_MAGIC = 0x48465231; // HFR1

    protected long clientId;
    protected long timestamp;
    private boolean hybridFixProtocolExtension;

    public ConnectionRequest() {
        reliability = Reliability.RELIABLE;
    }

    public ConnectionRequest(long clientId) {
        this(clientId, false);
    }

    public ConnectionRequest(long clientId, boolean hybridFixProtocolExtension) {
        this();
        this.clientId = clientId;
        this.timestamp = System.nanoTime();
        this.hybridFixProtocolExtension = hybridFixProtocolExtension;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(clientId);
        buf.writeLong(timestamp);
        buf.writeBoolean(false);
        if (hybridFixProtocolExtension) {
            buf.writeInt(HYBRIDFIX_PROTOCOL_EXTENSION_MAGIC);
        }
    }

    @Override
    public void decode(ByteBuf buf) {
        clientId = buf.readLong(); //client id
        timestamp = buf.readLong();
        buf.readBoolean(); //use security
        hybridFixProtocolExtension = buf.readableBytes() >= 4
                && buf.readInt() == HYBRIDFIX_PROTOCOL_EXTENSION_MAGIC;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasHybridFixProtocolExtension() {
        return hybridFixProtocolExtension;
    }

}
