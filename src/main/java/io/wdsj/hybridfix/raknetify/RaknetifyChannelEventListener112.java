package io.wdsj.hybridfix.raknetify;

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
        if (msg instanceof SPacketJoinGame || msg instanceof SPacketRespawn) {
            ctx.write(SynchronizationLayer.SYNC_REQUEST_OBJECT);
        }
        super.write(ctx, msg, promise);
    }
}
