package io.wdsj.hybridfix.entry.bukkit.hook.worldguard;

import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.util.Events;
import io.wdsj.hybridfix.api.bukkit.event.entity.EntityAttackByEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WGHookPvpListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttackPlayer(EntityAttackByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        if (!(damager instanceof Player) || !(victim instanceof Player)) return;
        Events.fireToCancel(event, new DamageEntityEvent(event, Cause.create(damager), victim));
    }
}
