package io.wdsj.hybridfix.mixin.api.bukkit.block;

import net.minecraft.util.math.BlockPos;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Works along with {@link BlockMixin}
 */
@Mixin(value = CraftBlock.class, remap = false)
public abstract class CraftBlockMixin {
    // @formatter:off
    @Shadow public abstract World getWorld();
    @Unique private BlockPos hybridFix$pos;
    // @formatter:on

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    public void init(CraftChunk chunk, int x, int y, int z, CallbackInfo ci) {
        this.hybridFix$pos = new BlockPos(x, y, z);
    }

    @Unique
    public boolean isBuildable() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().isSolid();
    }

    @Unique
    public boolean isBurnable() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().getCanBurn();
    }

    @Unique
    public boolean isReplaceable() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().isReplaceable();
    }

    @Unique
    public boolean isSolid() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().blocksMovement();
    }

    @Unique
    public boolean isCollidable() {
        net.minecraft.world.World world = ((CraftWorld) this.getWorld()).getHandle();
        return world.getBlockState(hybridFix$pos).getCollisionBoundingBox(world, hybridFix$pos) != null;
    }
}
