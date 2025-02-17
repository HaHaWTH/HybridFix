package io.wdsj.hybridfix.entry.bukkit.hook.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.EntityBlockFormEvent;

public class ResHookBlockFormListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onForm(BlockFormEvent event) {
        if (!(event instanceof EntityBlockFormEvent)) return;
        EntityBlockFormEvent eEvent = (EntityBlockFormEvent) event;

        if (!Flags.spread.isGlobalyEnabled()) return;
        Residence plugin = Residence.getInstance();
        if (plugin == null) return;
        if (plugin.isDisabledWorldListener(eEvent.getBlock().getWorld())) return;
        FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has(Flags.spread, true)) {
            event.setCancelled(true);
        }
    }
}
