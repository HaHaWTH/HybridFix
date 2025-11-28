package io.wdsj.hybridfix.entry.bukkit.hook.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class ResHookEntityChangeBlockListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent event) {
        Residence plugin = Residence.getInstance();
        if (plugin == null) return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld())) return;
        // Skip checks that Residence has already done
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (type == EntityType.ENDERMAN || type == EntityType.WITHER) return;
        // End

        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!ResidenceBlockListener.canBreakBlock(player, event.getBlock().getLocation(), false)) {
                event.setCancelled(true);
            }
            return;
        }

        FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has(Flags.destroy, true)) {
            event.setCancelled(true);
        }
    }
}
