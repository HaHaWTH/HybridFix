package io.wdsj.hybridfix.mixin.late.epic_siege_mod.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import funwayguy.epicsiegemod.ai.ESM_EntityAIGrief;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ESM_EntityAIGrief.class)
public abstract class ESM_EntityAIGriefMixin {
    @Shadow(remap = false)
    private EntityLiving entityLiving;

    @WrapOperation(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            )
    )
    private boolean canDestroyBlock(World instance, BlockPos blockPos, boolean b, Operation<Boolean> original) {
        if (EntityUtils.canDestroyBlock(instance, blockPos, this.entityLiving)) {
            return original.call(instance, blockPos, b);
        } else {
            return false;
        }
    }
}
