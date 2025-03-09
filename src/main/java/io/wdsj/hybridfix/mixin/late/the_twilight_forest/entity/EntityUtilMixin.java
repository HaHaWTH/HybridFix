package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.util.EntityUtil;

@Mixin(EntityUtil.class)
public abstract class EntityUtilMixin {
    @Inject(
            method = "canDestroyBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)Z",
            at = @At(
                    value = "HEAD"
            ),
            remap = false,
            cancellable = true
    )
    private static void callEvent(World world, BlockPos pos, IBlockState state, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        org.bukkit.entity.Entity bEntity = ((IEntityGetter) entity).getBukkitEntity();
        Block block = ((IWorldGetter) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(bEntity, block, Material.AIR, (byte) 0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
