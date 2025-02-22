package io.wdsj.hybridfix.entry.bukkit.hook.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.EntityBlockFormEvent;

public class ResHookBlockFormListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // Priority matters here, always higher than Residence
    public void onForm(BlockFormEvent event) {
        if (!(event instanceof EntityBlockFormEvent)) return;
        if (!Flags.spread.isGlobalyEnabled()) return;
        Residence plugin = Residence.getInstance();
        if (plugin == null) return;
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld())) return;
        // Skip checks that have already been done by Residence
        if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) return;
        final BlockState newState = event.getNewState();
        Material newType = newState.getType();
        if (newType == Material.SNOW || newType == Material.ICE || newType == Material.FROSTED_ICE) return;
        // End

        FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has(Flags.spread, true)) {
            event.setCancelled(true);
        }
    }
}
