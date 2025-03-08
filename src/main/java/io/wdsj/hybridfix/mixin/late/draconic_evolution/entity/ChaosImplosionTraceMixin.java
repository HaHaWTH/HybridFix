package io.wdsj.hybridfix.mixin.late.draconic_evolution.entity;

import com.brandon3055.draconicevolution.entity.ProcessChaosImplosion;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProcessChaosImplosion.ChaosImplosionTrace.class)
public abstract class ChaosImplosionTraceMixin {
    @WrapOperation(
            method = "updateProcess",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"
            ),
            remap = false
    )
    private boolean onSetBlockToAir(World instance, BlockPos pos, Operation<Boolean> original) {
        org.bukkit.World bWorld = ((IWorldGetter) instance).getWorld();
        Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        BlockState bs = bBlock.getState();
        bs.setType(Material.AIR);
        // noinspection deprecation
        bs.setRawData((byte) 0);
        BlockFormEvent event = new BlockFormEvent(bBlock, bs);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        return original.call(instance, pos);
    }
}
