package io.wdsj.hybridfix.mixin.late.the_twilight_forest.sapling;

import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.hybrid.HybridReflectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.event.world.StructureGrowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.block.BlockTFSapling;

import java.util.List;
import java.util.Random;

@Mixin(value = BlockTFSapling.class, remap = false)
public abstract class BlockTFSaplingMixin {
    @Inject(
            method = "grow",
            at = @At(
                    value = "HEAD"
            ),
            remap = true
    )
    public void onGrowHead(World worldIn, Random rand, BlockPos pos, IBlockState state, CallbackInfo ci) {
        HybridReflectionUtils.setCaptureTreeGeneration(worldIn, true);
    }

    @Inject(
            method = "grow",
            at = @At(
                    value = "RETURN"
            ),
            remap = true
    )
    // Afterglow >_<
    public void afterGrow(World worldIn, Random rand, BlockPos pos, IBlockState state, CallbackInfo ci) {
        HybridReflectionUtils.setCaptureTreeGeneration(worldIn, false);
        if (!worldIn.capturedBlockSnapshots.isEmpty()) {
            Location location = new Location(((IWorldGetter)worldIn).getWorld(), pos.getX(), pos.getY(), pos.getZ());
            List<BlockState> blockstates = new ObjectArrayList<>(worldIn.capturedBlockSnapshots.size());
            for (BlockSnapshot snapshot : worldIn.capturedBlockSnapshots) {
                blockstates.add(HybridReflectionUtils.newBlockStateFromBlockSnapshot(snapshot));
            }
            worldIn.capturedBlockSnapshots.clear();
            StructureGrowEvent event = new StructureGrowEvent(location, TreeType.TREE, false, null, blockstates);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                for (BlockState blockstate : blockstates) {
                    blockstate.update(true);
                }
            }
        }
    }
}
