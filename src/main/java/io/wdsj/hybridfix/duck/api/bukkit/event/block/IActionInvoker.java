package io.wdsj.hybridfix.duck.api.bukkit.event.block;

import io.wdsj.hybridfix.HybridFixServer;

/**
 * Duck interface for {@link org.bukkit.event.block.Action}.
 * You can cast to this from an {@link org.bukkit.event.block.Action} instance.
 */
@SuppressWarnings("unused")
public interface IActionInvoker {
    /**
     * Gets whether this action is a result of a left click.
     *
     * @return Whether it's a left click
     */
    default boolean isLeftClick() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }

    /**
     * Gets whether this action is a result of a right click.
     *
     * @return Whether it's a right click
     */
    default boolean isRightClick() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }
}
