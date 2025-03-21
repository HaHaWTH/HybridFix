package io.wdsj.hybridfix.mixin.late.thaumcraft.flux;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.common.entities.EntityFluxRift;

@Mixin(EntityFluxRift.class)
public abstract class EntityFluxRiftMixin extends Entity {
    public EntityFluxRiftMixin(World worldIn) {
        super(worldIn);
    }

    @Unique
    private static final boolean hybridFix$forceDisableFluxRiftGrief = Boolean.getBoolean("hybridfix.thaumcraft.forceDisableFluxRiftGrief");

    @WrapOperation(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;canCollideCheck(Lnet/minecraft/block/state/IBlockState;Z)Z"
            )
    )
    public boolean canCollideCheck(Block instance, IBlockState state, boolean hitIfLiquid, Operation<Boolean> original, @Local(ordinal = 0) BlockPos pos) {
        if (hybridFix$forceDisableFluxRiftGrief) {
            return false;
        }
        boolean originalVal = original.call(instance, state, hitIfLiquid);
        return EntityUtils.canDestroyBlock(this.world, pos, state, this) && originalVal;
    }
}
