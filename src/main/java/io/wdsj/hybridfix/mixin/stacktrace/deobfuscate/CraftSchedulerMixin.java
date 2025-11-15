package io.wdsj.hybridfix.mixin.stacktrace.deobfuscate;

import io.wdsj.hybridfix.util.ModCompatUtils;
import org.bukkit.craftbukkit.v1_12_R1.scheduler.CraftScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CraftScheduler.class, remap = false)
public abstract class CraftSchedulerMixin {
    @ModifyArg(
            method = "mainThreadHeartbeat",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/logging/Logger;log(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V"
            ),
            index = 2,
            require = 0
    )
    public Throwable deobfuscate(Throwable th) {
        if (th != null && ModCompatUtils.isCensoredASMInstalled()) {
            ModCompatUtils.censoredASM_deobfuscateThrowable(th);
        }
        return th;
    }
}
