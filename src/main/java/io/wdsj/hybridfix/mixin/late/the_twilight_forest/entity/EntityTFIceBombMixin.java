package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.block.BlockLiquid;
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
import twilightforest.entity.boss.EntityTFIceBomb;

@SuppressWarnings("deprecation")
@Mixin(EntityTFIceBomb.class)
public abstract class EntityTFIceBombMixin {

    @Redirect(
            method = "doTerrainEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            slice = @Slice(
                    from = @At(
                            value = "HEAD"
                    ),
                    to = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/block/material/Material;LAVA:Lnet/minecraft/block/material/Material;",
                            remap = true
                    )
            ),
            remap = false
    )
    public boolean redirectSetIce(World instance, BlockPos pos, IBlockState state) {
        org.bukkit.World bWorld = instance.getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        BlockState blockState = bBlock.getState();
        blockState.setType(Material.ICE);
        assert Blocks.ICE != null;
        final byte data = (byte) Blocks.ICE.getMetaFromState(state);
        blockState.setRawData(data);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IEntityGetter) this).getBukkitEntity(), bBlock, Material.ICE, data);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            blockState.update(true);
        }
        return true;
    }

    @Redirect(
            method = "doTerrainEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/block/material/Material;LAVA:Lnet/minecraft/block/material/Material;",
                            remap = true
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/World;isAirBlock(Lnet/minecraft/util/math/BlockPos;)Z",
                            remap = true
                    )
            ),
            remap = false
    )
    public boolean redirectSetObsidian(World instance, BlockPos pos, IBlockState state, @Local IBlockState lavaState) {
        org.bukkit.World bWorld = instance.getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        BlockState blockState = bBlock.getState();
        EntityChangeBlockEvent event;
        if (lavaState.getValue(BlockLiquid.LEVEL) == 0) {
            blockState.setType(Material.OBSIDIAN);
            assert Blocks.OBSIDIAN != null;
            final byte data = (byte) Blocks.OBSIDIAN.getMetaFromState(state);
            blockState.setRawData(data);
            event = new EntityChangeBlockEvent(((IEntityGetter) this).getBukkitEntity(), bBlock, Material.OBSIDIAN, data);
        } else {
            blockState.setType(Material.COBBLESTONE);
            event = new EntityChangeBlockEvent(((IEntityGetter) this).getBukkitEntity(), bBlock, Material.COBBLESTONE, (byte) 0);
        }
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            blockState.update(true);
        }
        return true;
    }

    @Redirect(
            method = "doTerrainEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/World;isAirBlock(Lnet/minecraft/util/math/BlockPos;)Z",
                            remap = true
                    )
            ),
            remap = false
    )
    public boolean redirectSetSnow(World instance, BlockPos pos, IBlockState state) {
        org.bukkit.World bWorld = instance.getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        BlockState blockState = bBlock.getState();
        blockState.setType(Material.SNOW);
        assert Blocks.SNOW_LAYER != null;
        final byte data = (byte) Blocks.SNOW_LAYER.getMetaFromState(state);
        blockState.setRawData(data);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IEntityGetter) this).getBukkitEntity(), bBlock, Material.SNOW, data);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            blockState.update(true);
        }
        return true;
    }
}
