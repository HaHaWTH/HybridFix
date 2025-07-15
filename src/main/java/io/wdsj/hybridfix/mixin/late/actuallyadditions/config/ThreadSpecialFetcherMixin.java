package io.wdsj.hybridfix.mixin.late.actuallyadditions.config;

import de.ellpeck.actuallyadditions.mod.misc.special.ThreadSpecialFetcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ThreadSpecialFetcher.class, remap = false)
public abstract class ThreadSpecialFetcherMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/ellpeck/actuallyadditions/mod/misc/special/ThreadSpecialFetcher;start()V"
            )
    )
    public void swallowCall(ThreadSpecialFetcher instance) {
        // no-op
    }

    /**
     * @author Creeam
     * @reason Stop fetching
     */
    @Overwrite
    public void run() {
        // no-op
    }
}
