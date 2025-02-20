package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import twilightforest.entity.EntityTFIceExploder;
import twilightforest.entity.EntityTFIceMob;

@Mixin(EntityTFIceExploder.class)
public abstract class EntityTFIceExploderMixin extends EntityTFIceMob {

    public EntityTFIceExploderMixin(World worldIn) {
        super(worldIn);
    }

    /*
    @Inject(
            method = "transformBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void transformLogicRewrite(BlockPos pos, CallbackInfo ci) {
        ci.cancel();
        IBlockState state = this.world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.getExplosionResistance(this) < 8.0F && state.getBlockHardness(this.world, pos) >= 0.0F) {
            int blockColor = state.getMapColor(this.world, pos).colorValue;
            org.bukkit.World bWorld = ((IWorldGetter) this.world).getWorld();
            org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            BlockState blockState = bBlock.getState();
            if (this.shouldTransformGlass(state, pos)) {
                blockState.setType(Material.STAINED_GLASS);
                final IBlockState newBs = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, this.getClosestDyeColor(blockColor));
                byte data = (byte) Blocks.STAINED_GLASS.getMetaFromState(newBs);
                blockState.setRawData(data);
                EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IEntityGetter)this).getBukkitEntity(), bBlock, Material.STAINED_GLASS, data);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    blockState.update(true);
                }
                // this.world.setBlockState(pos, Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, this.getClosestDyeColor(blockColor)));
            } else if (this.shouldTransformClay(state, pos)) {
                blockState.setType(Material.STAINED_CLAY);
                final IBlockState newBs = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, this.getClosestDyeColor(blockColor));
                byte data = (byte) Blocks.STAINED_HARDENED_CLAY.getMetaFromState(newBs);
                blockState.setRawData(data);
                EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IEntityGetter)this).getBukkitEntity(), bBlock, Material.STAINED_CLAY, data);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    blockState.update(true);
                }
                // his.world.setBlockState(pos, Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, this.getClosestDyeColor(blockColor)));
            }
        }
    }
     */

    @Redirect(
            method = "transformBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/block/Block;getExplosionResistance(Lnet/minecraft/entity/Entity;)F",
                            remap = true
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Ltwilightforest/entity/EntityTFIceExploder;shouldTransformClay(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)Z"
                    )
            ),
            remap = false,
            require = 1
    )
    public boolean redirectGlass(World instance, BlockPos pos, IBlockState state) {
        org.bukkit.World bWorld = ((IWorldGetter) instance).getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        BlockState blockState = bBlock.getState();
        blockState.setType(Material.STAINED_GLASS);
        final byte data = (byte) Blocks.STAINED_GLASS.getMetaFromState(state);
        blockState.setRawData(data);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IEntityGetter)this).getBukkitEntity(), bBlock, Material.STAINED_GLASS, data);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            blockState.update(true);
        }
        return true;
    }

    @Redirect(
            method = "transformBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Ltwilightforest/entity/EntityTFIceExploder;shouldTransformClay(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)Z"
                    )
            ),
            remap = false,
            require = 1
    )
    public boolean redirectClay(World instance, BlockPos pos, IBlockState state) {
        org.bukkit.World bWorld = ((IWorldGetter) instance).getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        BlockState blockState = bBlock.getState();
        blockState.setType(Material.STAINED_CLAY);
        final byte data = (byte) Blocks.STAINED_HARDENED_CLAY.getMetaFromState(state);
        blockState.setRawData(data);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IEntityGetter)this).getBukkitEntity(), bBlock, Material.STAINED_CLAY, data);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            blockState.update(true);
        }
        return true;
    }
}
