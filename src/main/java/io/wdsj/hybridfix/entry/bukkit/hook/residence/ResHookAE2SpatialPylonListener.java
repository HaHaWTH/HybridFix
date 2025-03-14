package io.wdsj.hybridfix.entry.bukkit.hook.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import io.wdsj.hybridfix.api.bukkit.event.applied_energistics_2.spatial.SpatialPylonTransferEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ResHookAE2SpatialPylonListener implements Listener {
    @EventHandler
    public void onSpatialPylonTransfer(SpatialPylonTransferEvent event) {
        Residence plugin = Residence.getInstance();
        if (plugin == null) return;
        World world = event.getWorld();
        // disabling event on world
        if (plugin.isDisabledWorldListener(world)) return;
        Location minLocation = event.getMin();
        Location maxLocation = event.getMax();
        int minX = minLocation.getBlockX(), minY = minLocation.getBlockY(), minZ = minLocation.getBlockZ();
        int maxX = maxLocation.getBlockX(), maxY = maxLocation.getBlockY(), maxZ = maxLocation.getBlockZ();
        final Location temp = new Location(world, minX, minY, minZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    temp.setX(x);
                    temp.setY(y);
                    temp.setZ(z);
                    FlagPermissions perms = plugin.getPermsByLoc(temp);
                    if (!perms.has(Flags.build, true)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
