package io.wdsj.hybridfix.mixin.raknetify.client;

import com.ishland.raknetify.common.util.PrefixUtil;
import io.wdsj.hybridfix.raknetify.GuiConnectingRakNetState;
import net.minecraft.client.multiplayer.GuiConnecting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiConnecting.class)
public abstract class GuiConnectingMixin implements GuiConnectingRakNetState {
    @Unique
    private boolean raknetify$useRakNet;
    @Unique
    private boolean raknetify$largeMtu;

    @ModifyArg(
            method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ServerData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ServerAddress;fromString(Ljava/lang/String;)Lnet/minecraft/client/multiplayer/ServerAddress;"
            ),
            index = 0
    )
    private String raknetify$stripServerDataPrefix(String address) {
        return capture(address);
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/GuiConnecting;connect(Ljava/lang/String;I)V"
            ),
            index = 0
    )
    private String raknetify$stripDirectPrefix(String host) {
        return capture(host);
    }

    @Unique
    private String capture(String address) {
        PrefixUtil.Info info = PrefixUtil.getInfo(address);
        this.raknetify$useRakNet = info.useRakNet();
        this.raknetify$largeMtu = info.largeMTU();
        return info.stripped();
    }

    @Override
    public boolean raknetify$useRakNet() {
        return this.raknetify$useRakNet;
    }

    @Override
    public boolean raknetify$largeMtu() {
        return this.raknetify$largeMtu;
    }
}
