package io.wdsj.hybridfix.mixin.raknetify;

import io.netty.channel.Channel;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketKeepAlive;
import network.ycc.raknet.RakNet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow @Final public NetworkManager netManager;
    @Shadow public EntityPlayerMP player;
    @Shadow private long field_194402_f;
    @Shadow private boolean field_194403_g;
    @Shadow private long field_194404_h;

    @Inject(method = "update", at = @At("HEAD"))
    private void raknetify$updateRakNetPing(CallbackInfo ci) {
        Channel channel = this.netManager.channel();
        if (RaknetifyConnectionUtil112.isRakNet(channel)) {
            this.field_194402_f = System.nanoTime() / 1_000_000L;
            RakNet.Config config = (RakNet.Config) channel.config();
            this.player.ping = (int) ((config.getRTTNanos() + config.getRTTStdDevNanos()) / 1_000_000L);
        }
    }

    @Inject(method = "processKeepAlive", at = @At("HEAD"), cancellable = true)
    private void raknetify$handleRakNetKeepAlive(CPacketKeepAlive packet, CallbackInfo ci) {
        if (!RaknetifyConnectionUtil112.isRakNet(this.netManager.channel())) {
            return;
        }

        if (this.field_194403_g && packet.getKey() == this.field_194404_h) {
            this.field_194403_g = false;
        }
        ci.cancel();
    }
}
