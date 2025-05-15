package io.wdsj.hybridfix.mixin.late.witchery.infusion.symbol;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.entity.EntityLivingBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.msrandom.witchery.infusion.symbol.ExpelliarmusSymbolEffect$onCollision$1")
public abstract class ExpelliarmusSymbolEffectMixin {
    @Dynamic
    @Inject(
            method = "accept(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/EntityLivingBase;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;",
                    remap = true
            ),
            remap = false,
            cancellable = true
    )
    public void onEntityDropItem(EntityLivingBase actor, EntityLivingBase target, CallbackInfo ci) {
        Entity bActor = ((IEntityGetter) actor).getBukkitEntity();
        Entity bTarget = ((IEntityGetter) target).getBukkitEntity();
        // noinspection deprecation
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(bActor, bTarget, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
