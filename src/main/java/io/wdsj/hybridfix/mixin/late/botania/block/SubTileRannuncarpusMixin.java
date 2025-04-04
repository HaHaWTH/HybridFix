package io.wdsj.hybridfix.mixin.late.botania.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.common.block.subtile.functional.SubTileRannuncarpus;

import java.util.Objects;

@Mixin(SubTileRannuncarpus.class)
public abstract class SubTileRannuncarpusMixin {
    @WrapOperation(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean onUpdate(World instance, BlockPos pos, IBlockState newState, int flags, Operation<Boolean> original, @Cancellable CallbackInfo ci, @Local(ordinal = 0, name = "pos") BlockPos superTilePos) {
        EntityPlayerMP serverPlayer = Objects.requireNonNull(HybridFixFakePlayer.get(instance, superTilePos).get());
        org.bukkit.World bWorld = ((IWorldGetter) instance).getWorld();
        Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Material bMaterial = CraftMagicNumbers.getMaterial(newState.getBlock());
        byte data = (byte) newState.getBlock().getMetaFromState(newState);
        CraftPlayer player = (CraftPlayer) ((IEntityGetter) serverPlayer).getBukkitEntity();
        // noinspection deprecation
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(player, bBlock, bMaterial, data);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
            return false;
        }
        return original.call(instance, pos, newState, flags);
    }
}
