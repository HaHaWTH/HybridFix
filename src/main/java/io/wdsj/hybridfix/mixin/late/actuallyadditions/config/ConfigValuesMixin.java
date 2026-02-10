package io.wdsj.hybridfix.mixin.late.actuallyadditions.config;

import de.ellpeck.actuallyadditions.mod.config.ConfigValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ConfigValues.class, remap = false)
public abstract class ConfigValuesMixin {
    @ModifyArg(
            method = "defineConfigValues",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/config/Configuration;get(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;II)Lnet/minecraftforge/common/config/Property;"
            ),
            index = 2
    )
    private static int modifyConfigValues(int defaultValue) {
        if (defaultValue == 11) {
            return 947 + 0xBADF00D;
        }
        return defaultValue;
    }
}
