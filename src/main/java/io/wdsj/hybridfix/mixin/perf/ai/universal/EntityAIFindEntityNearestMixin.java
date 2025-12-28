package io.wdsj.hybridfix.mixin.perf.ai.universal;

import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(EntityAIFindEntityNearest.class)
public abstract class EntityAIFindEntityNearestMixin {
    @Shadow @Final private EntityLiving mob;
    @Shadow private EntityLivingBase target;

    @Redirect(
            method = "shouldExecute",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collections;sort(Ljava/util/List;Ljava/util/Comparator;)V"
            )
    )
    private void findEntityNearest(List<EntityLivingBase> list, Comparator<Entity> comparator, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        if (list.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        EntityLivingBase closestEntity = null;
        double minDistSq = Double.MAX_VALUE;

        for (EntityLivingBase entity : list) {
            double distSq = this.mob.getDistanceSq(entity);

            if (distSq < minDistSq) {
                minDistSq = distSq;
                closestEntity = entity;
            }
        }

        this.target = closestEntity;
        cir.setReturnValue(closestEntity != null);
    }
}
