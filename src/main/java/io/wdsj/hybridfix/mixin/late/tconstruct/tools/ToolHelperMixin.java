package io.wdsj.hybridfix.mixin.late.tconstruct.tools;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.utils.ToolHelper;

@Mixin(ToolHelper.class)
public abstract class ToolHelperMixin {
    @WrapOperation(
            method = "attackEntity(Lnet/minecraft/item/ItemStack;Lslimeknights/tconstruct/library/tools/ToolCore;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D",
                    remap = true
            ),
            remap = false
    )
    private static double callEvent(IAttributeInstance instance, Operation<Double> original, @Cancellable CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) EntityLivingBase attacker, @Local(argsOnly = true, ordinal = 0) Entity targetEntity) {
        org.bukkit.entity.Entity damager = ((IEntityGetter) attacker).getBukkitEntity();
        org.bukkit.entity.Entity victim = ((IEntityGetter) targetEntity).getBukkitEntity();
        float base = original.call(instance).floatValue();
        // noinspection deprecation
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, base); // Damage doesn't matter, we just need to check if it's cancelled
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
        return event.getDamage();
    }
}
