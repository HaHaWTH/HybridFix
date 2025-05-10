package io.wdsj.hybridfix.mixin.late.industrial_foregoing.block;

import com.buuz135.industrial.tile.block.BlackHoleControllerBlockDeprecated;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlackHoleControllerBlockDeprecated.class)
public abstract class BlackHoleControllerBlockDeprecatedMixin {
    @Inject(
            method = "createRecipe",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void createRecipe(CallbackInfo ci) {
        ci.cancel();
    }
}
