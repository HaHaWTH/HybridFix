package io.wdsj.hybridfix.mixin.raknetify.client;

import io.wdsj.hybridfix.raknetify.RaknetifyDebugHud112;
import net.minecraft.client.gui.GuiOverlayDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class GuiOverlayDebugMixin {
    @Inject(method = "call", at = @At("RETURN"))
    private void raknetify$appendDebugLines(CallbackInfoReturnable<List<String>> cir) {
        RaknetifyDebugHud112.append(cir.getReturnValue()::add);
    }
}
