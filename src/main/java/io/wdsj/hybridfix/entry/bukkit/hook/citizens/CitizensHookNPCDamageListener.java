package io.wdsj.hybridfix.entry.bukkit.hook.citizens;

import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackByEntityEvent;
import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.ClickRedirectTrait;
import net.citizensnpcs.trait.CommandTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@SuppressWarnings("deprecation")
public class CitizensHookNPCDamageListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCAttacked(EntityAttackEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc == null) return;
        event.setCancelled(npc.isProtected());
        if (event instanceof EntityAttackByEntityEvent) {
            EntityAttackByEntityEvent damageEvent = (EntityAttackByEntityEvent) event;
            Bukkit.getPluginManager().callEvent(new NPCDamageByEntityEvent(npc, new EntityDamageByEntityEvent(damageEvent.getDamager(), npc.getEntity(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, damageEvent.getDamage())));
            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player)) {
                return;
            }
            Player damager = (Player) damageEvent.getDamager();
            if (npc.hasTrait(ClickRedirectTrait.class) && (npc = npc.getTraitNullable(ClickRedirectTrait.class).getRedirectNPC()) == null) {
                return;
            }
            NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
            Bukkit.getPluginManager().callEvent(leftClickEvent);
            if (npc.hasTrait(CommandTrait.class)) {
                npc.getTraitNullable(CommandTrait.class).dispatch(damager, CommandTrait.Hand.LEFT);
            }
        } else {
            Bukkit.getPluginManager().callEvent(new NPCDamageEvent(npc, new EntityDamageEvent(npc.getEntity(), EntityDamageEvent.DamageCause.CUSTOM, event.getDamage())));
        }
    }
}
