package io.wdsj.hybridfix.mixin.stacktrace.deobfuscate;

import io.wdsj.hybridfix.util.ModCompatUtils;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SimplePluginManager.class)
public abstract class SimplePluginManagerMixin {
    @ModifyArg(
            method = "fireEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/logging/Logger;log(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V"
            ),
            index = 2,
            remap = false
    )
    public Throwable deobfuscate(Throwable thrown) {
        if (ModCompatUtils.isCensoredASMInstalled()) {
            ModCompatUtils.censoredASM_deobfuscateThrowable(thrown);
        }
        return thrown;
    }
}
