package io.wdsj.hybridfix.mixin.late.epic_siege_mod.entities;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import funwayguy.epicsiegemod.handlers.entities.SpiderHandler;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(SpiderHandler.class)
public abstract class SpiderHandlerMixin {
    @WrapOperation(
            method = "onAttacked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            remap = false
    )
    private boolean wrapSetBlockState(World instance, BlockPos pos, IBlockState state, Operation<Boolean> original, @Local(argsOnly = true) LivingHurtEvent event) {
        assert Blocks.WEB != null;
        byte data = (byte) Blocks.WEB.getMetaFromState(state);
        org.bukkit.World bWorld = ((IWorldGetter) instance).getWorld();
        org.bukkit.block.Block bBlock = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Entity bEntity = ((IEntityGetter) Objects.requireNonNull(event.getSource().getTrueSource())).getBukkitEntity(); // Nullability has been checked by the original method body
        // noinspection deprecation
        EntityChangeBlockEvent bEvent = new EntityChangeBlockEvent(bEntity, bBlock, Material.WEB, data);
        Bukkit.getPluginManager().callEvent(bEvent);
        if (bEvent.isCancelled()) {
            return false;
        }
        return original.call(instance, pos, state);
    }
}
