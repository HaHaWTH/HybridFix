package io.wdsj.hybridfix.mixin.late.epic_siege_mod.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import funwayguy.epicsiegemod.ai.ESM_EntityAIDigging;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ESM_EntityAIDigging.class)
public abstract class ESM_EntityAIDiggingMixin {
    @Shadow(remap = false)
    private EntityLiving digger;

    @WrapOperation(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            )
    )
    private boolean checkCanDestroy(World instance, BlockPos blockPos, boolean b, Operation<Boolean> original, @Share("canDestroyBlock") LocalBooleanRef canDestroyBlock, @Local(name = "canHarvest") LocalBooleanRef canHarvest) {
        boolean val = EntityUtils.canDestroyBlock(instance, blockPos, this.digger);
        canDestroyBlock.set(val);
        canHarvest.set(canHarvest.get() && val);
        if (!val) return false;
        return original.call(instance, blockPos, b);
    }

    @WrapOperation(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/pathfinding/PathNavigate;setPath(Lnet/minecraft/pathfinding/Path;D)Z"
            )
    )
    private boolean checkSetPath(PathNavigate instance, Path path, double v, Operation<Boolean> original, @Share("canDestroyBlock") LocalBooleanRef canDestroyBlock) {
        if (!canDestroyBlock.get()) {
            return false;
        } else {
            return original.call(instance, path, v);
        }
    }
}
