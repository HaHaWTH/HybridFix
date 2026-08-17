package io.wdsj.hybridfix.raknetify;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.login.server.SPacketEnableCompression;

/** Preserves the upstream promise ordering while suppressing vanilla compression setup. */
public final class RaknetifyCompressionCompatibilityHandler112 extends ChannelDuplexHandler {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof SPacketEnableCompression) {
            promise.trySuccess();
            ctx.write(msg);
            ctx.pipeline().remove(this);
            return;
        }
        super.write(ctx, msg, promise);
    }
}
