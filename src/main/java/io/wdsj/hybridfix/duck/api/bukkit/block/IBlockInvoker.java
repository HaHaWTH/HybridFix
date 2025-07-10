package io.wdsj.hybridfix.duck.api.bukkit.block;

import io.wdsj.hybridfix.HybridFixServer;

/**
 * Duck interface for {@link org.bukkit.block.Block}.
 * All methods here behave same as <a href="https://jd.papermc.io/paper/1.21.7/org/bukkit/block/Block.html">PaperMC Javadoc</a>.
 * You can cast to this interface from a {@link org.bukkit.block.Block} instance to access the methods below.
 */
@SuppressWarnings("unused")
public interface IBlockInvoker {
    /**
     * Check if this block is solid
     * <p>
     * Determined by Minecraft, typically a block a player can use to place a new block to build things.
     * An example of a non-buildable block would be liquids, flowers, or fire
     *
     * @return true if block is buildable
     */
    default boolean isBuildable() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }

    /**
     * Check if this block is burnable
     * <p>
     * Determined by Minecraft, typically a block that fire can destroy (Wool, Wood)
     *
     * @return true if block is burnable
     */
    default boolean isBurnable() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }

    /**
     * Check if this block is replaceable
     * <p>
     * Determined by Minecraft, representing a block that is not AIR that you can still place a new block at, such as flowers.
     * @return true if block is replaceable
     */
    default boolean isReplaceable() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }

    /**
     * Check if this block is solid
     * <p>
     * Determined by Minecraft, typically a block a player can stand on and can't be passed through.
     * This API is faster and more accurate than accessing Material#isSolid as it avoids a material lookup and switch statement.
     * @return true if block is solid
     */
    default boolean isSolid() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }

    /**
     * Checks if this block is collidable.
     *
     * @return true if collidable
     */
    default boolean isCollidable() {
        AssertionError error = new AssertionError("Not Implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }
}
