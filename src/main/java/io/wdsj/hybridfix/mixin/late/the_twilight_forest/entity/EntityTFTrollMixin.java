package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.entity.EntityTFTroll;
import twilightforest.util.EntityUtil;

@Mixin(EntityTFTroll.class)
public abstract class EntityTFTrollMixin extends EntityMob implements IRangedAttackMob {
    public EntityTFTrollMixin(World worldIn) {
        super(worldIn);
    }

    @WrapWithCondition(
            method = "ripenBer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    remap = true
            ),
            remap = false
    )
    public boolean checkCanDestroyBlock(World instance, BlockPos pos, IBlockState state) {
        return EntityUtil.canDestroyBlock(instance, pos, state, this);
    }
}
