package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.EntityTFCubeOfAnnihilation;
import twilightforest.util.EntityUtil;

import javax.annotation.Nullable;

@Mixin(EntityTFCubeOfAnnihilation.class)
public abstract class EntityTFCubeOfAnnihilationMixin extends EntityThrowable {
    public EntityTFCubeOfAnnihilationMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "canAnnihilate",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    public void callEventOnCheck(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        @Nullable
        EntityLivingBase thrower = this.getThrower();

        if (thrower instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) thrower;
            org.bukkit.block.Block bukkitBlock = ((IWorldGetter)(this.world)).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            Player bukkitPlayer = (CraftPlayer) ((IEntityGetter)serverPlayer).getBukkitEntity();
            BlockBreakEvent event = new BlockBreakEvent(bukkitBlock, bukkitPlayer);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        } else {
            Entity entity = thrower != null ? thrower : this;
            final boolean checkResult = EntityUtil.canDestroyBlock(this.world, pos, state, entity);
            if (!checkResult) {
                cir.setReturnValue(false);
            }
        }
    }
}
