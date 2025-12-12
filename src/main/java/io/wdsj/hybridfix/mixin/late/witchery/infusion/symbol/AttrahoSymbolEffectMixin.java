package io.wdsj.hybridfix.mixin.late.witchery.infusion.symbol;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.msrandom.witchery.infusion.symbol.AttrahoSymbolEffect;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AttrahoSymbolEffect.class)
public abstract class AttrahoSymbolEffectMixin {
    @WrapOperation(
            method = "onCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/msrandom/witchery/util/EntityUtil;pullTowards(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;DD)V"
            ),
            remap = false
    )
    private void patchPullTowards(Entity entity, Vec3d target, double dy, double yy, Operation<Void> original, @Local(argsOnly = true) EntityLivingBase caster) {
        org.bukkit.entity.Entity bDamagee = entity.getBukkitEntity();
        org.bukkit.entity.Entity bCaster = caster.getBukkitEntity();
        // noinspection deprecation
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(bCaster, bDamagee, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            original.call(entity, target, dy, yy);
        }
    }
}
