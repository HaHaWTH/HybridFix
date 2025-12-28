package io.wdsj.hybridfix.mixin.perf.ai.universal;

import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(EntityAINearestAttackableTarget.class)
public abstract class EntityAINearestAttackableTargetMixin<T extends EntityLivingBase> extends EntityAITarget {
    @Shadow protected T targetEntity;

    public EntityAINearestAttackableTargetMixin(EntityCreature creature, boolean checkSight) {
        super(creature, checkSight);
    }

    @Redirect(
            method = "shouldExecute",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collections;sort(Ljava/util/List;Ljava/util/Comparator;)V"
            )
    )
    private void find(List<T> list, Comparator<Entity> comparator, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        if (list.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        T closestEntity = null;
        double minDistSq = Double.MAX_VALUE;

        for (T entity : list) {
            double distSq = this.taskOwner.getDistanceSq(entity);

            if (distSq < minDistSq) {
                minDistSq = distSq;
                closestEntity = entity;
            }
        }

        this.targetEntity = closestEntity;
        cir.setReturnValue(closestEntity != null);
    }
}
