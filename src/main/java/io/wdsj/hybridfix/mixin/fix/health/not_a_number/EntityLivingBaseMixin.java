package io.wdsj.hybridfix.mixin.fix.health.not_a_number;

import io.wdsj.hybridfix.HybridFix;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
    // @formatter:off
    @Shadow public abstract float getMaxHealth();
    // @formatter:on
    @Unique
    private static final boolean DEBUG_NAN_HEALTH = Boolean.getBoolean("hybridfix.debug.health.not_a_number");
    @ModifyVariable(
            method = "setHealth(F)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float setHealth(float health) {
        if (Float.isNaN(health)) {
            if (DEBUG_NAN_HEALTH) {
                HybridFix.LOGGER.warn("EntityLivingBase#setHealth called with NaN health", new Throwable());
            }
            return this.getMaxHealth();
        }
        return health;
    }
}
