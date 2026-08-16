package io.wdsj.hybridfix.mixin.raknetify;

import com.ishland.raknetify.common.util.ThreadLocalUtil;
import io.wdsj.hybridfix.raknetify.GuiConnectingRakNetState;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.UnknownHostException;

@Mixin(targets = "net.minecraft.client.multiplayer.GuiConnecting$1")
public abstract class GuiConnectingThreadMixin {
    @Shadow(remap = false) @Final private GuiConnecting this$0;

    @Dynamic
    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"
            )
    )
    private NetworkManager raknetify$connect(InetAddress address, int port, boolean nativeTransport) {
        GuiConnectingRakNetState state = (GuiConnectingRakNetState) this.this$0;
        if (!state.raknetify$useRakNet()) {
            return NetworkManager.createNetworkManagerAndConnect(address, port, nativeTransport);
        }
        try {
            ThreadLocalUtil.setInitializingRaknet(true);
            ThreadLocalUtil.setInitializingRaknetLargeMTU(state.raknetify$largeMtu());
            return NetworkManager.createNetworkManagerAndConnect(raknetify$normalizeDestination(address), port, nativeTransport);
        } finally {
            ThreadLocalUtil.setInitializingRaknet(false);
            ThreadLocalUtil.setInitializingRaknetLargeMTU(false);
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
