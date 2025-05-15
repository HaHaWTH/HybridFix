package io.wdsj.hybridfix.mixin.late.witchery.util;

import net.minecraft.entity.EntityLivingBase;
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
            at =  @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;setHealth(F)V",
                    remap = true
            ),
            remap = false
    )
    private static void swallowSetHealth(EntityLivingBase instance, float health) {
        // no-op
    }
}
