package io.wdsj.hybridfix.mixin.api.bukkit.block;

import org.bukkit.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Block.class, remap = false)
public interface BlockMixin {
    @Unique
    default boolean isSolid() {
        return true;
    }
}
