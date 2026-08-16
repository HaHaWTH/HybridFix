package network.ycc.raknet.packet;

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;

public class ServerHandshake extends SimpleFramedPacket {

    private static final byte[] HYBRIDFIX_EXTENSION_ADDRESS = new byte[] {72, 70, 82, 49}; // HFR1
    private static final int HYBRIDFIX_EXTENSION_PORT = 23123;

    private InetSocketAddress clientAddr;
    private long timestamp;
    private int nExtraAddresses;
    private boolean hybridFixProtocolExtension;

    public ServerHandshake() {
        reliability = Reliability.RELIABLE;
    }

    public ServerHandshake(InetSocketAddress clientAddr, long timestamp) {
        this(clientAddr, timestamp, 20);
    }

    public ServerHandshake(InetSocketAddress clientAddr, long timestamp, int nExtraAddresses) {
        this(clientAddr, timestamp, nExtraAddresses, false);
    }

    public ServerHandshake(InetSocketAddress clientAddr, long timestamp, int nExtraAddresses,
            boolean hybridFixProtocolExtension) {
        this();
        this.clientAddr = clientAddr;
        this.timestamp = timestamp;
        this.nExtraAddresses = nExtraAddresses;
        this.hybridFixProtocolExtension = hybridFixProtocolExtension;
    }

    @Override
    public void encode(ByteBuf buf) {
        writeAddress(buf, clientAddr);
        buf.writeShort(0);
        for (int i = 0; i < nExtraAddresses; i++) {
            writeAddress(buf);
        }
        if (hybridFixProtocolExtension) {
            buf.writeByte(4);
            buf.writeInt(~0x48465231);
            buf.writeShort(HYBRIDFIX_EXTENSION_PORT);
        }
        buf.writeLong(timestamp);
        buf.writeLong(System.currentTimeMillis());
    }

    @Override
    public void decode(ByteBuf buf) {
        clientAddr = readAddress(buf);
        buf.readShort();
        for (nExtraAddresses = 0; buf.readableBytes() > 16; nExtraAddresses++) {
            InetSocketAddress extraAddress = readAddress(buf);
            if (extraAddress.getPort() == HYBRIDFIX_EXTENSION_PORT
                    && java.util.Arrays.equals(extraAddress.getAddress().getAddress(), HYBRIDFIX_EXTENSION_ADDRESS)) {
                hybridFixProtocolExtension = true;
            }
        }
        timestamp = buf.readLong();
        timestamp = buf.readLong();
    }

    public InetSocketAddress getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(InetSocketAddress clientAddr) {
        this.clientAddr = clientAddr;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getnExtraAddresses() {
        return nExtraAddresses;
    }

    public void setnExtraAddresses(int nExtraAddresses) {
        this.nExtraAddresses = nExtraAddresses;
    }

    public boolean hasHybridFixProtocolExtension() {
        return hybridFixProtocolExtension;
    }

}
