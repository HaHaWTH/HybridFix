package io.wdsj.hybridfix.entry.bukkit.hook.residence.v6;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Why this is needed: Since Residence 6.0.0.0, they moved the BlockPlaceEvent into 1.17+ listeners,
 * this change breaks compatibility with all old versions.
 * @see <a href="https://github.com/Zrips/Residence/blob/master/src/main/java/com/bekvon/bukkit/residence/listeners/ResidenceListener1_17.java#L54">ResidenceListener1_17</a>
 */
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
