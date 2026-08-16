package io.wdsj.hybridfix.mixin.raknetify.client;

import com.ishland.raknetify.common.util.PrefixUtil;
import com.ishland.raknetify.common.util.ThreadLocalUtil;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ServerPinger.class)
public abstract class ServerPingerMixin {
    @ModifyArg(
            method = {"ping", "tryCompatibilityPing"},
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerAddress;fromString(Ljava/lang/String;)Lnet/minecraft/client/multiplayer/ServerAddress;"
            ),
            index = 0
    )
    private String raknetify$stripPrefix(String address) {
        return PrefixUtil.getInfo(address).stripped();
    }

    @Redirect(
            method = "ping",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"
            )
    )
    private NetworkManager raknetify$pingRakNet(
            InetAddress address, int port, boolean nativeTransport, ServerData server) {
        PrefixUtil.Info info = PrefixUtil.getInfo(server.serverIP);
        if (!info.useRakNet()) {
            return NetworkManager.createNetworkManagerAndConnect(address, port, nativeTransport);
        }
        try {
            ThreadLocalUtil.setInitializingRaknet(true);
            ThreadLocalUtil.setInitializingRaknetLargeMTU(info.largeMTU());
            return NetworkManager.createNetworkManagerAndConnect(raknetify$normalizeDestination(address), port, nativeTransport);
        } finally {
            ThreadLocalUtil.setInitializingRaknet(false);
            ThreadLocalUtil.setInitializingRaknetLargeMTU(false);
        }
    }

    @Inject(method = "tryCompatibilityPing", at = @At("HEAD"), cancellable = true)
    private void raknetify$skipLegacyTcpPing(ServerData server, CallbackInfo ci) {
        if (PrefixUtil.getInfo(server.serverIP).useRakNet()) {
            ci.cancel();
        }
    }

    @Unique
    private static InetAddress raknetify$normalizeDestination(InetAddress address) {
        if (!(address instanceof Inet6Address) || !address.isAnyLocalAddress()) {
            return address;
        }
        try {
            return InetAddress.getByName("::1");
        } catch (UnknownHostException impossibleForIpv6Literal) {
            throw new IllegalStateException("Unable to resolve the IPv6 loopback literal", impossibleForIpv6Literal);
        }
    }
}
