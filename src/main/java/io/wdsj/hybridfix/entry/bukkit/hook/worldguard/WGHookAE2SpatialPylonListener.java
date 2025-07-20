package io.wdsj.hybridfix.entry.bukkit.hook.worldguard;

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import io.wdsj.hybridfix.api.bukkit.event.applied_energistics_2.spatial.SpatialPylonTransferEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WGHookAE2SpatialPylonListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTransfer(SpatialPylonTransferEvent event) {
        WorldGuardPlugin plugin = WorldGuardPlugin.inst();
        if (plugin == null) return;
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
        }
        if (wcfg.useRegions) {
            // noinspection deprecation
            GlobalRegionManager globalRegionManager = plugin.getGlobalRegionManager();
            for (Location location : event.getAffectedLocations()) {
                // noinspection deprecation
                if (!globalRegionManager.allows(DefaultFlag.BUILD, location)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}