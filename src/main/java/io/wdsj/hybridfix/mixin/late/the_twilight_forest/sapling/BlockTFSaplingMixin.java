package io.wdsj.hybridfix.mixin.late.the_twilight_forest.sapling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.reflection.HybridReflectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.event.world.StructureGrowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.block.BlockTFSapling;

import java.util.List;
import java.util.Random;

@Mixin(value = BlockTFSapling.class)
public abstract class BlockTFSaplingMixin {
    @Unique
    private boolean hybridFix$isBoneMeal = true;
    @Inject(
            method = "updateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/block/BlockTFSapling;grow(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"
            )
    )
    private void beforeNaturalGrow(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        hybridFix$isBoneMeal = false;
    }

    @Inject(
            method = "updateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/block/BlockTFSapling;grow(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterNaturalGrow(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        HybridReflectionUtils.setCaptureTreeGeneration(worldIn, false);
        hybridFix$isBoneMeal = true;
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

    @WrapOperation(
            method = "grow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/feature/WorldGenerator;generate(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    public boolean onGenerate(WorldGenerator instance, World world, Random random, BlockPos blockPos, Operation<Boolean> original, @Share("canGen") LocalBooleanRef canGen) {
        HybridReflectionUtils.setCaptureTreeGeneration(world, true);
        boolean result = original.call(instance, world, random, blockPos);
        canGen.set(result);
        return result;
    }

    @Inject(
            method = "grow",
            at = @At(
                    value = "HEAD"
            )
    )
    public void setTypeOnGrow(World world, Random rand, BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (hybridFix$isBoneMeal) {
            world.captureBlockSnapshots = false; // Check ForgeHooks, hybrids are disgusting
            world.capturedBlockSnapshots.clear();
            HybridReflectionUtils.setTreeType(TreeType.TREE);
        }
    }

    @Inject(
            method = "grow",
            at = @At(
                    value = "RETURN"
            )
    )
    // Afterglow >_<
    public void afterGrow(World world, Random rand, BlockPos pos, IBlockState state, CallbackInfo ci, @Share("canGen") LocalBooleanRef canGen) {
        if (!canGen.get()) {
            HybridReflectionUtils.setTreeType(null);
            HybridReflectionUtils.setCaptureTreeGeneration(world, false);
            List<BlockSnapshot> pending = new ObjectArrayList<>();
            pending.addAll(world.capturedBlockSnapshots);
            for (BlockSnapshot snapshot : pending) {
                snapshot.restore(true);
            }
            world.capturedBlockSnapshots.clear();
        }
    }
}
