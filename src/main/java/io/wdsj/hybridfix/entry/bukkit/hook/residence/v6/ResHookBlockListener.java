package io.wdsj.hybridfix.entry.bukkit.hook.residence.v6;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ResHookBlockListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!Flags.place.isGlobalyEnabled())
            return;

        if (ResidenceBlockListener.canPlaceBlock(event.getPlayer(), event.getBlock(), true))
            return;

        event.setCancelled(true);
    }
}
