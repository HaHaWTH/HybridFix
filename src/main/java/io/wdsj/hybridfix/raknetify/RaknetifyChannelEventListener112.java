package io.wdsj.hybridfix.raknetify;

import com.ishland.raknetify.common.connection.RakNetSimpleMultiChannelCodec;
import com.ishland.raknetify.common.connection.SynchronizationLayer;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketRespawn;

public final class RaknetifyChannelEventListener112 extends ChannelDuplexHandler {
    public static final String NAME = "raknetify-1_12-event-listener";

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        final boolean respawn = msg instanceof SPacketRespawn;
        if (msg instanceof SPacketJoinGame || respawn) {
            ctx.write(SynchronizationLayer.SYNC_REQUEST_OBJECT);
        }
        super.write(ctx, msg, promise);
        if (respawn) {
            // 1.12 has no post-respawn command-tree packet to restart the
            // multichannel codec. This ping is queued after sync and respawn.
            ctx.write(RakNetSimpleMultiChannelCodec.SIGNAL_START_MULTICHANNEL);
        }
    }
}
