package io.wdsj.hybridfix.mixin.late.the_twilight_forest.item;

import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.hybrid.HybridReflectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import twilightforest.item.ItemTFMagicBeans;

import java.util.List;

@Mixin(ItemTFMagicBeans.class)
public abstract class ItemTFMagicBeansMixin {

    @Inject(
            method = "makeHugeStalk",
            at = @At(
                    value = "HEAD"
            ),
            remap = false
    )
    public void startCapture(World world, BlockPos pos, int minY, int maxY, CallbackInfo ci) {
        HybridReflectionUtils.setCaptureTreeGeneration(world, true);
    }

    @Inject(
            method = "makeHugeStalk",
            at = @At(
                    value = "RETURN"
            ),
            remap = false
    )
    public void stopCaptureAndCallEvent(World world, BlockPos pos, int minY, int maxY, CallbackInfo ci) {
        HybridReflectionUtils.setCaptureTreeGeneration(world, false);
        if (!world.capturedBlockSnapshots.isEmpty()) {
            Location location = new Location(((IWorldGetter)world).getWorld(), pos.getX(), pos.getY(), pos.getZ());
            List<BlockState> blockstates = new ObjectArrayList<>(world.capturedBlockSnapshots.size());
            for (BlockSnapshot snapshot : world.capturedBlockSnapshots) {
                blockstates.add(HybridReflectionUtils.newBlockStateFromBlockSnapshot(snapshot));
            }
            world.capturedBlockSnapshots.clear();
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
