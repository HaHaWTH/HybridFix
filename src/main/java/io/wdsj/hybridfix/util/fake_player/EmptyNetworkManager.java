package io.wdsj.hybridfix.util.fake_player;

import io.netty.channel.*;
import net.minecraft.network.*;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(EnumPacketDirection flag) {
        super(flag);
        this.channel = new EmptyChannel(null);
        this.socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
    }

    @Override
    public boolean isChannelOpen() {
        return true;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext p_channelActive_1_) {
    }

    @Override
    public void handleDisconnection() {
    }

    @Override
    public void sendPacket(@NotNull Packet<?> packet) {
    }

    public static class EmptyChannel extends AbstractChannel {
        private final ChannelConfig config = new DefaultChannelConfig(this);

        public EmptyChannel(Channel parent) {
            super(parent);
        }

        public ChannelConfig config() {
            this.config.setAutoRead(true);
            return this.config;
        }

        protected void doBeginRead() {
        }

        protected void doBind(SocketAddress arg0) {
        }

        protected void doClose() {
        }

        protected void doDisconnect() {
        }

        protected void doWrite(ChannelOutboundBuffer arg0) {
        }

        public boolean isActive() {
            return true;
        }

        protected boolean isCompatible(EventLoop arg0) {
            return true;
        }

        public boolean isOpen() {
            return true;
        }

        protected SocketAddress localAddress0() {
            return null;
        }

        public ChannelMetadata metadata() {
            return new ChannelMetadata(true);
        }

        protected AbstractChannel.AbstractUnsafe newUnsafe() {
            return null;
        }

        protected SocketAddress remoteAddress0() {
            return null;
        }
    }
}
