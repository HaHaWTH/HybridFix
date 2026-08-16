package io.wdsj.hybridfix.raknetify;

import com.ishland.raknetify.common.Constants;
import com.ishland.raknetify.common.connection.MultiChannelingStreamingCompression;
import com.ishland.raknetify.common.connection.RakNetConnectionUtil;
import com.ishland.raknetify.common.connection.RakNetSimpleMultiChannelCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import network.ycc.raknet.RakNet;
import io.wdsj.hybridfix.config.Settings;

public final class RaknetifyConnectionUtil112 {
    public static final String PACKET_CAPTURE_NAME = "raknetify-1_12-packet-capture";
    public static final String COMPRESSION_COMPAT_NAME = "raknetify-1_12-compression-compat";

    private RaknetifyConnectionUtil112() {
    }

    public static boolean isRakNet(Channel channel) {
        return channel != null && channel.config() instanceof RakNet.Config;
    }

    /** Must run before Minecraft installs its application pipeline. */
    public static void initChannel(Channel channel) {
        if (!isRakNet(channel)) {
            return;
        }
        RakNetConnectionUtil.initChannel(
                channel,
                Settings.raknetify.enableZstdCompression,
                Settings.raknetify.zstdCompressionLevel,
                Settings.raknetify.zstdCompressionThreshold);
        channel.pipeline().addAfter(
                MultiChannelingStreamingCompression.NAME,
                RakNetSimpleMultiChannelCodec.NAME,
                new RakNetSimpleMultiChannelCodec(Constants.RAKNET_GAME_PACKET_ID));
    }

    /** Must run after Minecraft installs encoder/decoder/packet_handler. */
    public static void postInitChannel(Channel channel) {
        if (!isRakNet(channel)) {
            return;
        }

        ChannelPipeline pipeline = channel.pipeline();
        replaceWithNoop(pipeline, "timeout");
        replaceWithNoop(pipeline, "splitter");
        replaceWithNoop(pipeline, "prepender");

        MultiChannellingPacketCapture112 capture = new MultiChannellingPacketCapture112();
        pipeline.addAfter("encoder", PACKET_CAPTURE_NAME, capture);
        pipeline.get(RakNetSimpleMultiChannelCodec.class)
                .addHandler(capture.getForgeEntitySpawnHandler())
                .addHandler(capture.getCustomPayloadHandler())
                .addHandler(capture.getCaptureBasedHandler());
        pipeline.addLast(COMPRESSION_COMPAT_NAME, new RaknetifyCompressionCompatibilityHandler112());
        pipeline.addBefore("packet_handler", RaknetifyChannelEventListener112.NAME,
                new RaknetifyChannelEventListener112());
    }

    private static void replaceWithNoop(ChannelPipeline pipeline, String name) {
        ChannelHandler existing = pipeline.get(name);
        if (existing != null) {
            pipeline.replace(existing, name, new ChannelDuplexHandler());
        }
    }

    public static DatagramChannel datagramForSocketChannel(Class<? extends SocketChannel> socketChannel) {
        if (socketChannel == NioSocketChannel.class) {
            return new NioDatagramChannel();
        }
        if (socketChannel == EpollSocketChannel.class) {
            return new EpollDatagramChannel();
        }
        throw new UnsupportedOperationException("Unsupported Minecraft socket channel: " + socketChannel.getName());
    }
}
