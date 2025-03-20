package io.wdsj.hybridfix.mixin.late.epic_siege_mod.ai;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
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

    @WrapWithCondition(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            )
    )
    private boolean canDestroyBlock(World instance, BlockPos pos, boolean dropBlock) {
        return EntityUtils.canDestroyBlock(instance, pos, this.entityLiving);
    }
}
