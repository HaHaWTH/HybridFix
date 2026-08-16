package io.wdsj.hybridfix.raknetify;

import com.ishland.raknetify.common.connection.RakNetSimpleMultiChannelCodec;
import com.ishland.raknetify.common.connection.multichannel.CustomPayloadChannel;
import com.ishland.raknetify.common.util.MathUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;

@ChannelHandler.Sharable
public final class MultiChannellingPacketCapture112 extends ChannelOutboundHandlerAdapter {
    private Packet<?> packet;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        this.packet = msg instanceof Packet<?> ? (Packet<?>) msg : null;
        try {
            ctx.write(msg, promise);
        } finally {
            this.packet = null;
        }
    }

    public RakNetSimpleMultiChannelCodec.OverrideHandler getCustomPayloadHandler() {
        return new CustomPayloadChannel.OverrideHandler(ignored ->
                packet instanceof SPacketCustomPayload || packet instanceof CPacketCustomPayload);
    }

    /**
     * Forge 1.12.2 sends {@code FMLMessage.EntitySpawnMessage} as discriminator 2
     * on the {@code FML} custom-payload channel. Keep it on the same RakNet order
     * channel as the entity movement packets which may immediately follow it.
     */
    public RakNetSimpleMultiChannelCodec.OverrideHandler getForgeEntitySpawnHandler() {
        return (buf, suppressWarning) -> {
            if (!(packet instanceof SPacketCustomPayload) && !(packet instanceof CPacketCustomPayload)) {
                return 0;
            }

            ByteBuf copy = buf.slice();
            MathUtil.readVarInt(copy); // Minecraft packet id
            if (!"FML".equals(MathUtil.readString(copy)) || !copy.isReadable()) {
                return 0;
            }
            return copy.readUnsignedByte() == 2 ? 2 : 0;
        };
    }

    public RakNetSimpleMultiChannelCodec.OverrideHandler getCaptureBasedHandler() {
        return (ByteBuf buf, boolean suppressWarning) ->
                RaknetifyMultiChannel112.getPacketChannelOverride(packet, suppressWarning);
    }
}
