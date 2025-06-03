package io.wdsj.hybridfix.mixin.late.witchery.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.msrandom.witchery.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Setting health directly will break all Bukkit events.
 */
@SuppressWarnings("deprecation")
@Mixin(EntityUtil.class)
public abstract class EntityUtilMixin {
    @Redirect(
            method = "instantDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;setHealth(F)V",
                    remap = true
            ),
            remap = false
    )
    private static void swallowSetHealth(EntityLivingBase instance, float health) {
        // no-op
    }

    /* Prevent NaN health */
    @WrapOperation(
            method = "instantDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
                    remap = true
            ),
            remap = false
    )
    private static boolean wrapAttackEntityFrom(EntityLivingBase instance, DamageSource damageSource, float damage, Operation<Boolean> original) {
        boolean result = original.call(instance, damageSource, damage);
        if (result) instance.setHealth(0.0F);
        return result;
    }
}
