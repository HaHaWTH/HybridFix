package io.wdsj.hybridfix.mixin.late.botania.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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
import vazkii.botania.api.subtile.SubTileFunctional;
import vazkii.botania.common.block.subtile.functional.SubTileRannuncarpus;

import java.util.Objects;

@Mixin(SubTileRannuncarpus.class)
public abstract class SubTileRannuncarpusMixin extends SubTileFunctional {
    @WrapOperation(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;canPlaceBlockAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean onUpdate(net.minecraft.block.Block block, World worldIn, BlockPos pos, Operation<Boolean> original, @Local(name = "stateToPlace") IBlockState newState) {
        final boolean originalVal = original.call(block, worldIn, pos);
        if (!originalVal) return false;
        EntityPlayerMP serverPlayer = Objects.requireNonNull(HybridFixFakePlayer.get(worldIn, supertile.getPos(), "botania-SubTileRannuncarpus").get());
        org.bukkit.World bWorld = worldIn.getWorld();
        Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Material bMaterial = CraftMagicNumbers.getMaterial(newState.getBlock());
        byte data = (byte) newState.getBlock().getMetaFromState(newState);
        CraftPlayer player = (CraftPlayer) serverPlayer.getBukkitEntity();
        // noinspection deprecation
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(player, bBlock, bMaterial, data);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}
