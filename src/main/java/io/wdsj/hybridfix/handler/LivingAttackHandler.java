package io.wdsj.hybridfix.handler;

import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackByEntityEvent;
import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackEvent;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class LivingAttackHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        Entity bVictim = ((IEntityGetter) victim).getBukkitEntity();
        DamageSource source = event.getSource();
        net.minecraft.entity.Entity directDamager = source.getImmediateSource();
        Entity bDirectDamager = directDamager == null ? null : ((IEntityGetter) directDamager).getBukkitEntity();
        net.minecraft.entity.Entity realDamager = source.getTrueSource();
        Entity bRealDamager = realDamager == null ? null : ((IEntityGetter) realDamager).getBukkitEntity();
        EntityAttackEvent attackEvent;
        float damage = event.getAmount();
        String damageType = source.getDamageType();
        if (bRealDamager != null) {
            attackEvent = new EntityAttackByEntityEvent(bVictim, damage, bRealDamager, damageType);
        } else if (bDirectDamager != null) {
            attackEvent = new EntityAttackByEntityEvent(bVictim, damage, bDirectDamager, damageType);
        } else {
            attackEvent = new EntityAttackEvent(bVictim, damage, damageType);
        }
        Bukkit.getPluginManager().callEvent(attackEvent);
        if (attackEvent.isCancelled()) {
            event.setCanceled(true);
        }
    }
}
