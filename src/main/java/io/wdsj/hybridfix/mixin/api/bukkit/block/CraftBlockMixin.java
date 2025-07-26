package io.wdsj.hybridfix.mixin.api.bukkit.block;

import io.wdsj.hybridfix.duck.api.bukkit.block.IBlockInvoker;
import io.wdsj.hybridfix.state.block.BlockEntitySnapshotState;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import org.bukkit.World;
import org.bukkit.block.BlockState;
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
@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = CraftBlock.class, remap = false)
public abstract class CraftBlockMixin implements IBlockInvoker {
    // @formatter:off
    @Shadow public abstract World getWorld();
    @Shadow protected abstract Block getNMSBlock();
    @Shadow public abstract BlockState getState();
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
    @Override
    public boolean isBuildable() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().isSolid();
    }

    @Unique
    @Override
    public boolean isBurnable() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().getCanBurn();
    }

    @Unique
    @Override
    public boolean isReplaceable() {
        net.minecraft.world.World world = ((CraftWorld) this.getWorld()).getHandle();
        return this.getNMSBlock().isReplaceable(world, hybridFix$pos);
    }

    @Unique
    @Override
    public boolean isSolid() {
        return ((CraftWorld) this.getWorld()).getHandle().getBlockState(hybridFix$pos).getMaterial().blocksMovement();
    }

    @Unique
    @Override
    public boolean isCollidable() {
        net.minecraft.world.World world = ((CraftWorld) this.getWorld()).getHandle();
        return world.getBlockState(hybridFix$pos).getCollisionBoundingBox(world, hybridFix$pos) != null;
    }

    @Unique
    @Override
    public BlockState getState(boolean useSnapshot) {
        try {
            BlockEntitySnapshotState.ENABLE_SNAPSHOT = useSnapshot;
            return this.getState();
        } finally {
            BlockEntitySnapshotState.ENABLE_SNAPSHOT = true;
        }
    }
}
