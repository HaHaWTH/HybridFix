package io.wdsj.hybridfix.mixin.raknetify;

import com.ishland.raknetify.common.connection.RakNetSimpleMultiChannelCodec;
import io.netty.channel.Channel;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketJoinGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin {
    @Shadow public abstract NetworkManager getNetworkManager();

    @Inject(method = "handleJoinGame", at = @At("RETURN"))
    private void raknetify$startMultichannel(SPacketJoinGame packet, CallbackInfo ci) {
        Channel channel = this.getNetworkManager().channel();
        if (RaknetifyConnectionUtil112.isRakNet(channel)) {
            channel.eventLoop().execute(() -> channel.write(RakNetSimpleMultiChannelCodec.SIGNAL_START_MULTICHANNEL));
        }
    }
}
