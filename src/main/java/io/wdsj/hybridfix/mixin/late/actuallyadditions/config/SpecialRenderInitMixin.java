package io.wdsj.hybridfix.mixin.late.actuallyadditions.config;

import de.ellpeck.actuallyadditions.mod.misc.special.SpecialRenderInit;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpecialRenderInit.class, remap = false)
public abstract class SpecialRenderInitMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;register(Ljava/lang/Object;)V"
            )
    )
    public void swallowCall(EventBus instance, Object eventType) {
        // no-op
    }
}
