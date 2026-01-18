package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.entity.EntityTFChainBlock;

@Mixin(EntityTFChainBlock.class)
public abstract class EntityTFChainBlockMixin extends EntityThrowable {

    public EntityTFChainBlockMixin(World worldIn) {
        super(worldIn);
    }

    @WrapOperation(
            method = "affectBlocksInAABB",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean wrapDestroyBlock(World instance, BlockPos pos, boolean dropBlock, Operation<Boolean> original, @Share("harvestCalled") LocalBooleanRef isHarvestCalled, @Local(ordinal = 0) IBlockState state) {
        @Nullable
        EntityLivingBase thrower = this.getThrower();
        final boolean isPlayer = thrower instanceof EntityPlayerMP;
        if (isHarvestCalled.get() || (!isPlayer && EntityUtils.callBlockBreakEventForEntity(instance, pos, state, thrower != null ? thrower : this))) {
            return original.call(instance, pos, dropBlock);
        }
        return false;
    }

    @WrapOperation(
            method = "affectBlocksInAABB",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;harvestBlock(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/item/ItemStack;)V",
                    remap = true
            ),
            remap = false
    )
    public void wrapHarvest(Block instance, World world, EntityPlayer player, BlockPos pos, IBlockState iBlockState, TileEntity te, ItemStack item, Operation<Void> original, @Share("harvestCalled") LocalBooleanRef isHarvestCalled) {
        if (!(player instanceof EntityPlayerMP)) {
            original.call(instance, world, player, pos, iBlockState, te, item);
            isHarvestCalled.set(true);
            return;
        }
        org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Player bukkitPlayer = (CraftPlayer) player.getBukkitEntity();
        BlockBreakEvent event = new BlockBreakEvent(bukkitBlock, bukkitPlayer);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            isHarvestCalled.set(false);
            return;
        }
        if (event.isDropItems()) {
            original.call(instance, world, player, pos, iBlockState, te, item);
            isHarvestCalled.set(true);
        } else {
            world.destroyBlock(pos, false);
            isHarvestCalled.set(false);
        }
    }
}
