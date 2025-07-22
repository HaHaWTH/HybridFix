package io.wdsj.hybridfix.handler;

import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackByEntityEvent;
import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackEvent;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

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
        handleAttackEvent(event, source, bVictim, bDirectDamager, bRealDamager);
        if (Settings.compatModeForAttackBridge && !event.isCanceled()) {
            handleAttackEventCompat(event, source, bVictim, bDirectDamager, bRealDamager);
        }
    }


    private void handleAttackEvent(LivingAttackEvent event, DamageSource source, Entity bVictim, Entity bDirectDamager, Entity bRealDamager) {
        float damage = event.getAmount();
        EntityAttackEvent attackEvent;
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

    @SuppressWarnings("deprecation")
    private void handleAttackEventCompat(LivingAttackEvent event, DamageSource ignored, Entity bVictim, Entity bDirectDamager, Entity bRealDamager) {
        float damage = event.getAmount();
        EntityDamageEvent attackEvent;
        if (bRealDamager != null) {
            attackEvent = new EntityDamageByEntityEvent(bRealDamager, bVictim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage);
        } else if (bDirectDamager != null) {
            attackEvent = new EntityDamageByEntityEvent(bDirectDamager, bVictim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage);
        } else {
            attackEvent = new EntityDamageEvent(bVictim, EntityDamageEvent.DamageCause.CUSTOM, damage);
        }
        Bukkit.getPluginManager().callEvent(attackEvent);
        if (attackEvent.isCancelled()) {
            event.setCanceled(true);
        }
    }
}
