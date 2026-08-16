package io.wdsj.hybridfix.mixin.raknetify.client;

import com.ishland.raknetify.common.Constants;
import com.ishland.raknetify.common.connection.RakNetConnectionUtil;
import com.ishland.raknetify.common.connection.RaknetifyEventLoops;
import com.ishland.raknetify.common.util.ThreadLocalUtil;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.network.NetworkManager;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.client.channel.RakNetClientThreadedChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerConnectMixin {
    @Unique private static final ThreadLocal<EventLoopGroup> raknetify$parentEventLoop = new ThreadLocal<>();

    @Redirect(
            method = "createNetworkManagerAndConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/bootstrap/Bootstrap;group(Lio/netty/channel/EventLoopGroup;)Lio/netty/bootstrap/AbstractBootstrap;",
                    remap = false
            )
    )
    private static AbstractBootstrap<?, ?> raknetify$selectGroup(Bootstrap bootstrap, EventLoopGroup minecraftGroup) {
        raknetify$parentEventLoop.set(minecraftGroup);
        return ThreadLocalUtil.isInitializingRaknet()
                ? bootstrap.group(RaknetifyEventLoops.DEFAULT_CLIENT_EVENT_LOOP_GROUP.get())
                : bootstrap.group(minecraftGroup);
    }

    @Redirect(
            method = "createNetworkManagerAndConnect",
            at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;channel(Ljava/lang/Class;)Lio/netty/bootstrap/AbstractBootstrap;", remap = false))
    private static AbstractBootstrap<?, ?> raknetify$selectChannel(
            Bootstrap bootstrap, Class<? extends SocketChannel> socketChannelClass) {
        EventLoopGroup parentGroup = raknetify$parentEventLoop.get();
        raknetify$parentEventLoop.remove();
        if (!ThreadLocalUtil.isInitializingRaknet()) {
            return bootstrap.channel(socketChannelClass);
        }
        if (parentGroup == null) {
            throw new IllegalStateException("Missing Minecraft parent event loop for RakNet connection");
        }

        final boolean largeMtu = ThreadLocalUtil.isInitializingRaknetLargeMTU();
        return bootstrap.channelFactory(() -> {
            DatagramChannel datagram = RaknetifyConnectionUtil112.datagramForSocketChannel(socketChannelClass);
            datagram.config().setOption(ChannelOption.IP_TOS, RakNetConnectionUtil.DEFAULT_IP_TOS);
            int mtu = largeMtu ? Constants.LARGE_MTU : Constants.DEFAULT_MTU;
            datagram.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(mtu + 512).maxMessagesPerRead(128));

            RakNetClientThreadedChannel channel = new RakNetClientThreadedChannel(() -> datagram);
            RakNet.config(channel).setMTU(mtu);
            channel.setProvidedParentEventLoop(parentGroup.next());
            return channel;
        });
    }
}
