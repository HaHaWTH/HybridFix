package io.wdsj.hybridfix.mixin.late.tconstruct.tools;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

@Mixin(ToolHelper.class)
public abstract class ToolHelperMixin {
    @Unique
    private static final boolean hybridFix$supersedeVanillaEvent = Boolean.getBoolean("hybridfix.tconstruct.supersedeVanillaEvent");
    @Inject(
            method = "attackEntity(Lnet/minecraft/item/ItemStack;Lslimeknights/tconstruct/library/tools/ToolCore;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lslimeknights/tconstruct/library/utils/TinkerUtil;getTraitsOrdered(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"
            ),
            remap = false,
            cancellable = true
    )
    private static void callEvent(ItemStack stack, ToolCore tool, EntityLivingBase attacker, Entity targetEntity, Entity projectileEntity, boolean applyCooldown, CallbackInfoReturnable<Boolean> cir) {
        org.bukkit.entity.Entity damager = ((IEntityGetter) attacker).getBukkitEntity();
        org.bukkit.entity.Entity victim = ((IEntityGetter) targetEntity).getBukkitEntity();
        float base = (float) attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        // noinspection deprecation
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, base); // Damage doesn't matter, we just need to check if it's cancelled
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(hybridFix$supersedeVanillaEvent);
        }
    }
}
