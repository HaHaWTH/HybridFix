package io.wdsj.hybridfix.mixin.raknetify;

import com.ishland.raknetify.common.Constants;
import com.ishland.raknetify.common.connection.RakNetConnectionUtil;
import com.ishland.raknetify.common.connection.RaknetifyEventLoops;
import com.ishland.raknetify.common.util.NetworkInterfaceListener;
import com.ishland.raknetify.common.util.ThreadLocalUtil;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import network.ycc.raknet.server.channel.RakNetServerChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Mixin(NetworkSystem.class)
public abstract class NetworkSystemMixin {
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    public volatile boolean isAlive;
    @Shadow
    @Final
    private List<ChannelFuture> endpoints;

    @Shadow
    public abstract void addEndpoint(InetAddress address, int port) throws IOException;

    @Unique
    private Consumer<NetworkInterfaceListener.InterfaceAddressChangeEvent> raknetify$interfaceListener;

    @Inject(method = "addEndpoint", at = @At("HEAD"))
    private void raknetify$bindUdp(InetAddress address, int port, CallbackInfo ci) throws IOException {
        if (ThreadLocalUtil.isInitializingRaknet()) {
            return;
        }

        try {
            ThreadLocalUtil.setInitializingRaknet(true);
            int configuredPort = Integer.getInteger("raknetify.fabric.portOverride", Settings.raknetify.portOverride);
            boolean override = configuredPort > 0 && configuredPort < 65535;
            int udpPort = override ? configuredPort : port;
            if (address != null) {
                bindOne(address, udpPort);
                return;
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    bindOne(addresses.nextElement(), udpPort);
                }
            }

            if (this.raknetify$interfaceListener == null) {
                this.raknetify$interfaceListener = event -> {
                    if (!this.isAlive) {
                        NetworkInterfaceListener.removeListener(this.raknetify$interfaceListener);
                        return;
                    }
                    try {
                        ThreadLocalUtil.setInitializingRaknet(true);
                        if (event.added()) {
                            bindOne(event.address(), udpPort);
                        } else {
                            synchronized (this.endpoints) {
                                for (Iterator<ChannelFuture> iterator = this.endpoints.iterator(); iterator.hasNext(); ) {
                                    ChannelFuture endpoint = iterator.next();
                                    SocketAddress local = endpoint.channel().localAddress();
                                    if (local instanceof InetSocketAddress
                                            && event.address().equals(((InetSocketAddress) local).getAddress())
                                            && endpoint.channel() instanceof RakNetServerChannel) {
                                        endpoint.channel().close();
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    } finally {
                        ThreadLocalUtil.setInitializingRaknet(false);
                    }
                };
                NetworkInterfaceListener.addListener(event ->
                        this.server.addScheduledTask(() -> this.raknetify$interfaceListener.accept(event)));
            }
        } finally {
            ThreadLocalUtil.setInitializingRaknet(false);
        }
    }

    @Unique
    private void bindOne(InetAddress address, int port) {
        HybridFix.LOGGER.info("Starting Raknetify UDP endpoint on {}:{}", address, port);
        try {
            this.addEndpoint(address, port);
        } catch (IOException exception) {
            HybridFix.LOGGER.warn("Raknetify: failed to bind UDP endpoint on {}:{}: {}", address, port, exception.getMessage());
        }
    }

    @Redirect(
            method = "addEndpoint",
            at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/ServerBootstrap;group(Lio/netty/channel/EventLoopGroup;)Lio/netty/bootstrap/ServerBootstrap;", remap = false))
    private ServerBootstrap raknetify$selectGroup(ServerBootstrap bootstrap, EventLoopGroup minecraftGroup) {
        if (!ThreadLocalUtil.isInitializingRaknet()) {
            return bootstrap.group(minecraftGroup);
        }
        boolean epoll = Epoll.isAvailable() && this.server.shouldUseNativeTransport();
        return bootstrap.group(epoll
                ? RaknetifyEventLoops.EPOLL_EVENT_LOOP_GROUP.get()
                : RaknetifyEventLoops.NIO_EVENT_LOOP_GROUP.get());
    }

    @Redirect(
            method = "addEndpoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/bootstrap/ServerBootstrap;channel(Ljava/lang/Class;)Lio/netty/bootstrap/AbstractBootstrap;",
                    remap = false
            )
    )
    private AbstractBootstrap<?, ?> raknetify$selectChannel(
            ServerBootstrap bootstrap, Class<? extends ServerSocketChannel> minecraftChannel) {
        if (!ThreadLocalUtil.isInitializingRaknet()) {
            return bootstrap.channel(minecraftChannel);
        }
        boolean epoll = Epoll.isAvailable() && this.server.shouldUseNativeTransport();
        return bootstrap.channelFactory(() -> {
            RakNetServerChannel channel = new RakNetServerChannel(() -> {
                DatagramChannel datagram = epoll ? new EpollDatagramChannel() : new NioDatagramChannel();
                datagram.config().setOption(ChannelOption.SO_REUSEADDR, true);
                datagram.config().setOption(ChannelOption.IP_TOS, RakNetConnectionUtil.DEFAULT_IP_TOS);
                datagram.config().setRecvByteBufAllocator(
                        new FixedRecvByteBufAllocator(Constants.LARGE_MTU + 512).maxMessagesPerRead(128));
                return datagram;
            });
            channel.setProvidedApplicationEventLoop(RaknetifyEventLoops.DEFAULT_EVENT_LOOP_GROUP.get().next());
            return channel;
        });
    }
}
