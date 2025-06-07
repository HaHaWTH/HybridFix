package io.wdsj.hybridfix.mixin.stacktrace.deobfuscate;

import io.wdsj.hybridfix.util.ModCompatUtils;
import org.spigotmc.AsyncCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AsyncCatcher.class, remap = false)
public class AsyncCatcherMixin {
    @Redirect(
            method = "catchOp",
            at = @At(
                    value = "NEW",
                    target = "java/lang/IllegalStateException"
            )
    )
    private static IllegalStateException deobfuscate(String message) {
        IllegalStateException exception = new IllegalStateException(message);
        if (ModCompatUtils.isCensoredASMInstalled()) {
            ModCompatUtils.censoredASM_deobfuscateThrowable(exception);
        }
        return exception;
    }
}