package io.wdsj.hybridfix.entry.bukkit.hook.worldguard;

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.EntityBlockFormEvent;

public class WGHookBlockFormListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // Higher than WorldGuard
    public void onBlockForm(BlockFormEvent event) {
        WorldGuardPlugin plugin = WorldGuardPlugin.inst();
        if (plugin == null) return;
        Block block = event.getBlock();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(block.getWorld());

        // Skip checks that have already been done by WorldGuard
        if (event instanceof EntityBlockFormEvent && ((EntityBlockFormEvent) event).getEntity() instanceof Snowman) return;
        final BlockState newBs = event.getNewState();
        Material newType = newBs.getType();

        if (newType == Material.SNOW || newType == Material.ICE || newType == Material.FROSTED_ICE) return;
        // End

        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }

        if (wcfg.useRegions) {
            // noinspection deprecation
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.BLOCK_BREAK, block.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
