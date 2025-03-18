package io.wdsj.hybridfix.mixin.late.infernal_mobs.modifiers;

import atomicstryker.infernalmobs.common.mods.MM_Webber;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MM_Webber.class)
public abstract class MM_WebberMixin {
    @WrapOperation(
            method = "tryAbility",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean wrapAbility(World instance, BlockPos pos, IBlockState state, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) EntityLivingBase mob) {
        assert Blocks.WEB != null;
        byte data = (byte) Blocks.WEB.getMetaFromState(state);
        org.bukkit.World bWorld = ((IWorldGetter) instance).getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Entity bEntity = ((IEntityGetter) mob).getBukkitEntity();
        // noinspection deprecation
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(bEntity, bBlock, Material.WEB, data);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        return original.call(instance, pos, state);
    }
}
