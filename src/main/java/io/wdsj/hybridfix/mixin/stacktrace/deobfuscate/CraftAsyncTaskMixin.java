package io.wdsj.hybridfix.mixin.stacktrace.deobfuscate;

import io.wdsj.hybridfix.util.ModCompatUtils;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Pseudo
@Mixin(targets = {"org.bukkit.craftbukkit.v1_12_R1.scheduler.CraftAsyncTask"})
public abstract class CraftAsyncTaskMixin {
    @Dynamic
    @ModifyVariable(
            method = "run()V",
            at = @At(
                    value = "STORE"
            ),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Ljava/util/logging/Logger;log(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V"
                    )
            ),
            ordinal = 0
    )
    public Throwable deobfuscate(Throwable th) {
        if (th != null && ModCompatUtils.isCensoredASMInstalled()) {
            ModCompatUtils.censoredASM_deobfuscateThrowable(th);
        }
        return th;
    }
}
