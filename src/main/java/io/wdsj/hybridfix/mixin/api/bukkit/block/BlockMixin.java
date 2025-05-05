package io.wdsj.hybridfix.mixin.api.bukkit.block;

import org.bukkit.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implemented by {@link CraftBlockMixin}.
 */
@SuppressWarnings("AddedMixinMembersNamePattern")
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
}
