package io.wdsj.hybridfix.entry.bukkit.listener;

import io.wdsj.hybridfix.config.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplodeListener implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        String worldName = event.getEntity().getWorld().getName();
        for (String s : Settings.bukkitPluginConfig.antiExplodeWorlds) {
            if (s.equalsIgnoreCase(worldName)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        String worldName = event.getBlock().getWorld().getName();
        for (String s : Settings.bukkitPluginConfig.antiExplodeWorlds) {
            if (s.equalsIgnoreCase(worldName)) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
