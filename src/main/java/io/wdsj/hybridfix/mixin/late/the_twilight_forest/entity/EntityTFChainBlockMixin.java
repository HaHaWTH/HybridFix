package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.entity.EntityTFChainBlock;
import twilightforest.util.EntityUtil;

import javax.annotation.Nullable;

@Mixin(EntityTFChainBlock.class)
public abstract class EntityTFChainBlockMixin extends EntityThrowable {

    public EntityTFChainBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Unique
    private boolean hybridFix$isPlayerHarvestCalled = false;
    @WrapOperation(
            method = "affectBlocksInAABB",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean wrapDestroyBlock(World instance, BlockPos pos, boolean dropBlock, Operation<Boolean> original) {
        @Nullable
        EntityLivingBase thrower = this.getThrower();
        final boolean isPlayer = thrower instanceof EntityPlayerMP;
        if (hybridFix$isPlayerHarvestCalled || (!isPlayer && EntityUtil.canDestroyBlock(instance, pos, thrower != null ? thrower : this))) {
            hybridFix$isPlayerHarvestCalled = false;
            return original.call(instance, pos, dropBlock);
        }
        hybridFix$isPlayerHarvestCalled = false;
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
    public void wrapHarvest(Block instance, World world, EntityPlayer player, BlockPos pos, IBlockState iBlockState, TileEntity te, ItemStack item, Operation<Void> original) {
        if (!(player instanceof EntityPlayerMP)) {
            original.call(instance, world, player, pos, iBlockState, te, item);
            hybridFix$isPlayerHarvestCalled = true;
            return;
        }
        org.bukkit.block.Block bukkitBlock = ((IWorldGetter)(world)).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Player bukkitPlayer = (CraftPlayer) ((IEntityGetter)player).getBukkitEntity();
        BlockBreakEvent event = new BlockBreakEvent(bukkitBlock, bukkitPlayer);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            hybridFix$isPlayerHarvestCalled = false;
            return;
        }
        if (event.isDropItems()) {
            original.call(instance, world, player, pos, iBlockState, te, item);
            hybridFix$isPlayerHarvestCalled = true;
        } else {
            world.destroyBlock(pos, false);
            hybridFix$isPlayerHarvestCalled = false;
        }
    }
}
