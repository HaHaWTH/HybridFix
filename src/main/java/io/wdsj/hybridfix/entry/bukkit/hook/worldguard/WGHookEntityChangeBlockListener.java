package io.wdsj.hybridfix.entry.bukkit.hook.worldguard;

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class WGHookEntityChangeBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChange(EntityChangeBlockEvent event) {
        WorldGuardPlugin plugin = WorldGuardPlugin.inst();
        if (plugin == null) return;
        Entity ent = event.getEntity();
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (event instanceof EntityBreakDoorEvent || ent instanceof Enderman || ent instanceof Wither) return; // Don't check these entities as WG has already done that.

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(ent.getWorld());
        if (wcfg.disableMobDamage) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            // noinspection deprecation
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.MOB_DAMAGE, location)) {
                event.setCancelled(true);
            }
        }
    }
}
