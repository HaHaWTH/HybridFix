package io.wdsj.hybridfix.mixin.api.bukkit.block;

import io.wdsj.hybridfix.HybridFixServer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implemented by {@link CraftBlockMixin}.
 */
@SuppressWarnings({"AddedMixinMembersNamePattern", "unused"})
@Mixin(value = Block.class, remap = false)
public interface BlockMixin {
    @Unique
    default boolean isBuildable() {
        return true;
    }

    @Unique
    default boolean isBurnable() {
        return false;
    }

    @Unique
    default boolean isReplaceable() {
        return false;
    }

    @Unique
    default boolean isSolid() {
        return true;
    }

    @Unique
    default boolean isCollidable() {
        return true;
    }

    @Unique
    default BlockState getState(boolean useSnapshot) {
        AssertionError error = new AssertionError("Not Implemented.");
        HybridFixServer.createServerDump(error);
        throw error;
    }
}
