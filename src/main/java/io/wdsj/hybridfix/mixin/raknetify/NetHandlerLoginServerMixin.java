package io.wdsj.hybridfix.mixin.raknetify;

import com.ishland.raknetify.common.connection.MultiChannelingStreamingCompression;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = NetHandlerLoginServer.class, priority = 900)
public abstract class NetHandlerLoginServerMixin {
    @Shadow @Final public NetworkManager networkManager;

    @Redirect(
            method = "tryAcceptPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getNetworkCompressionThreshold()I"
            )
    )
    private int raknetify$disableVanillaCompression(MinecraftServer server) {
        if (RaknetifyConnectionUtil112.isRakNet(this.networkManager.channel())) {
            MultiChannelingStreamingCompression compression = this.networkManager.channel().pipeline().get(MultiChannelingStreamingCompression.class);
            if (compression != null && compression.isActive()) {
                HybridFix.LOGGER.info("Raknetify: preventing vanilla compression while streaming compression is active");
                return -1;
            }
        }
        return server.getNetworkCompressionThreshold();
    }
}
