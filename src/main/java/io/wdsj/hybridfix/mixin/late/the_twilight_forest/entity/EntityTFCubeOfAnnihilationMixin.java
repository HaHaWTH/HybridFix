package io.wdsj.hybridfix.mixin.late.the_twilight_forest.entity;

import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.EntityTFCubeOfAnnihilation;

@Mixin(EntityTFCubeOfAnnihilation.class)
public abstract class EntityTFCubeOfAnnihilationMixin extends EntityThrowable {
    public EntityTFCubeOfAnnihilationMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "canAnnihilate",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    public void callEventOnCheck(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        @Nullable
        EntityLivingBase thrower = this.getThrower();

        if (thrower instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) thrower;
            boolean isCancelled = EntityUtils.callBlockBreakEventForPlayer(this.world, pos, state, serverPlayer);
            if (isCancelled) {
                cir.setReturnValue(false);
            }
        } else {
            Entity entity = thrower != null ? thrower : this;
            final boolean cancelled = EntityUtils.callBlockBreakEventForEntity(this.world, pos, state, entity);
            if (cancelled) {
                cir.setReturnValue(false);
            }
        }
    }
}
