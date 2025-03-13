package io.wdsj.hybridfix.api.bukkit.event.applied_energistics_2.spatial;

import io.wdsj.hybridfix.api.annotation.ModEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a Spatial Pylon attempts to transfer.
 */
@SuppressWarnings("unused")
@ModEvent("appliedenergistics2")
public class SpatialPylonTransferEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final World world;
    private final Location min;
    private final Location max;

    public SpatialPylonTransferEvent(World world, Location min, Location max) {
        this.world = world;
        this.min = min;
        this.max = max;
    }

    /**
     * Gets the world in which the activation area is located.
     *
     * @return the world in which the activation area is located
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the minimum point of the activation area.
     * The location returned should <b>NOT</b> be modified!
     *
     * @return the minimum point of the activation area
     */
    public Location getMin() {
        return min;
    }

    /**
     * Gets the maximum point of the activation area.
     * The location returned should <b>NOT</b> be modified!
     *
     * @return the maximum point of the activation area
     */
    public Location getMax() {
        return max;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
