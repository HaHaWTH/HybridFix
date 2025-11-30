package io.wdsj.hybridfix.mixin.fix.activation_range;

import io.wdsj.hybridfix.duck.fix.activation_range.EntityEARAccessor;
import net.minecraft.entity.Entity;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ActivationRange.class, remap = false)
public abstract class ActivationRangeMixin {
    @Inject(
            method = "checkIfActive",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void checkIfActive(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (((EntityEARAccessor) (entity)).hybridFix$isIgnoringEAR()) {
            cir.setReturnValue(true);
        }
    }
}
