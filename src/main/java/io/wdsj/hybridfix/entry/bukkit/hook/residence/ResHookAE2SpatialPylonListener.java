package io.wdsj.hybridfix.entry.bukkit.hook.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import io.wdsj.hybridfix.api.bukkit.event.applied_energistics_2.spatial.SpatialPylonTransferEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class ResHookAE2SpatialPylonListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onSpatialPylonTransfer(SpatialPylonTransferEvent event) {
        Residence plugin = Residence.getInstance();
        if (plugin == null) return;
        World world = event.getWorld();
        // disabling event on world
        if (plugin.isDisabledWorldListener(world)) return;
        List<Location> locations = event.getAffectedLocations();
        for (Location location : locations) {
            FlagPermissions perms = plugin.getPermsByLoc(location);
            if (!perms.has(Flags.build, true)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
