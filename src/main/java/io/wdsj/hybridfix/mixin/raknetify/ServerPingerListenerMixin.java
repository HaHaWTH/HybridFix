package io.wdsj.hybridfix.mixin.raknetify;

import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import network.ycc.raknet.RakNet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.network.ServerPinger$1")
public abstract class ServerPingerListenerMixin {
    @Shadow(remap = false) @Final private NetworkManager val$networkmanager;
    @Shadow(remap = false) @Final private ServerData val$server;

    @Inject(method = "handleServerInfo", at = @At("RETURN"))
    private void raknetify$useRakNetRtt(CallbackInfo ci) {
        if (RaknetifyConnectionUtil112.isRakNet(this.val$networkmanager.channel())) {
            RakNet.Config config = (RakNet.Config) this.val$networkmanager.channel().config();
            this.val$server.pingToServer = config.getRTTNanos() / 1_000_000L;
        }
    }
}
