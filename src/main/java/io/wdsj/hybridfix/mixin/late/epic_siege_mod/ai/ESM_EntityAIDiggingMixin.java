package io.wdsj.hybridfix.mixin.late.epic_siege_mod.ai;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
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

    @WrapWithCondition(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            )
    )
    private boolean checkCanDestroy(World instance, BlockPos pos, boolean dropBlock, @Share("canDestroyBlock") LocalBooleanRef canDestroyBlock, @Local(ordinal = 0) LocalBooleanRef canHarvest) {
        boolean val = EntityUtils.canDestroyBlock(instance, pos, this.digger);
        canDestroyBlock.set(val);
        canHarvest.set(canHarvest.get() && val);
        return val;
    }

    @WrapWithCondition(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/pathfinding/PathNavigate;setPath(Lnet/minecraft/pathfinding/Path;D)Z"
            )
    )
    private boolean checkSetPath(PathNavigate instance, Path path, double pathentityIn, @Share("canDestroyBlock") LocalBooleanRef canDestroyBlock) {
        return canDestroyBlock.get();
    }
}
