package io.wdsj.hybridfix.api.bukkit.event.applied_energistics_2.spatial;

import io.wdsj.hybridfix.api.annotation.ModEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a Spatial Pylon attempts to swap a region.
 * Listeners can cancel this event to stop the transition.
 * The destination region is unknown at this point.
 * <p>
 * The region to be transferred is defined by {@code min} and {@code max}, which extend one block outward from the actual cube boundaries:
 * <pre>
 *            max .
 *         +----+
 *        /    /|
 *       /    / |
 *      /____/  |
 *      |    |  +
 *      |    | /
 *      +----+
 * min .
 * </pre>
 * In this diagram, {@code min} is one block outside the bottom-front-left corner, and {@code max} is one block outside the top-back-right corner of the cube.
 * The actual transferred region is the inner cube enclosed within these outer bounds, you may want to offset them by 1 when using these locations.
 */
@SuppressWarnings("unused")
@ModEvent("appliedenergistics2")
public class SpatialPylonTransferEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final World world;
    private final Location min;
    private final Location max;

    @ApiStatus.Internal
    public SpatialPylonTransferEvent(World world, Location min, Location max) {
        this.world = world;
        this.min = min;
        this.max = max;
    }

    /**
     * Gets which world the Spatial Pylon is in.
     *
     * @return the world
     */
    @NotNull
    public World getWorld() {
        return world;
    }

    /**
     * Gets the minimum point of the region to transfer.
     * The location returned is <b>NOT</b> intended to be modified!
     *
     * @return the minimum point of the region
     */
    @NotNull
    public Location getMin() {
        return min;
    }

    /**
     * Gets the maximum point of the region to transfer.
     * The location returned is <b>NOT</b> intended to be modified!
     *
     * @return the maximum point of the region
     */
    @NotNull
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