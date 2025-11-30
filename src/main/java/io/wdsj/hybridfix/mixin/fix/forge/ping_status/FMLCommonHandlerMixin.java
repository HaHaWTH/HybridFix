package io.wdsj.hybridfix.mixin.fix.forge.ping_status;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FMLCommonHandler.class)
public class FMLCommonHandlerMixin {
    @Inject(
            method = "handleServerHandshake",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/text/TextComponentString;<init>(Ljava/lang/String;)V", args = "ldc=Server is still starting! Please wait before reconnecting."
            ),
            cancellable = true,
            require = 0
    )
    public void handleServerHandshake(C00Handshake packet, NetworkManager manager, CallbackInfoReturnable<Boolean> cir) {
        if (packet.getRequestedState() == EnumConnectionState.STATUS) {
            manager.closeChannel(new TextComponentString("Server is still starting! Please wait before reconnecting."));
            cir.setReturnValue(false);
        }
    }
}
