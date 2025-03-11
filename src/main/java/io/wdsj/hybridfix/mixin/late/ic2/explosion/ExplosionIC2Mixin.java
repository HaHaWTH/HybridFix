package io.wdsj.hybridfix.mixin.late.ic2.explosion;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import ic2.core.ExplosionIC2;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
 * IC2 is too stupid. D:
 */
@Mixin(ExplosionIC2.class)
public abstract class ExplosionIC2Mixin {

    @Shadow(remap = false) @Final private Entity exploder;

    @Shadow(remap = false) @Final private World worldObj;

    @Redirect(
            method = "doExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBlockExploded(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/Explosion;)V"
            ),
            remap = false
    )
    public void swallowMethod(Block instance, World world, BlockPos pos, Explosion explosion) {
        // no-op
    }

    @WrapOperation(
            method = "doExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;",
                    remap = true
            ),
            remap = false
    )
    public Block wrapExplode(IBlockState instance, Operation<Block> original, @Local BlockPos.MutableBlockPos pos, @Share("isEventCancelled") LocalBooleanRef isEventCancelled) {
        org.bukkit.World bWorld = ((IWorldGetter) this.worldObj).getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        org.bukkit.entity.Entity entity = this.exploder != null ? ((IEntityGetter) this.exploder).getBukkitEntity() : null;
        boolean cancelled;
        if (entity != null) {
            // noinspection deprecation
            EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity, bBlock, Material.AIR, (byte) 0);
            Bukkit.getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
            isEventCancelled.set(cancelled);
        } else {
            BlockState blockState = bBlock.getState();
            blockState.setType(Material.AIR);
            // noinspection deprecation
            blockState.setRawData((byte) 0);
            BlockFormEvent event = new BlockFormEvent(bBlock, blockState);
            Bukkit.getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
            isEventCancelled.set(cancelled);
        }
        Block block = original.call(instance);
        if (!cancelled) {
            block.onBlockExploded(this.worldObj, pos, (ExplosionIC2) (Object) this);
        }
        return block;
    }

    @ModifyExpressionValue(
            method = "doExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;canDropFromExplosion(Lnet/minecraft/world/Explosion;)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean returnIfCancelled(boolean original, @Share("isEventCancelled") LocalBooleanRef isEventCancelled) {
        return !isEventCancelled.get() && original;
    }
}
