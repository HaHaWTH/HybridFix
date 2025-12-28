package io.wdsj.hybridfix.mixin.perf.ai.universal;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityAIFindEntityNearestPlayer.class)
public abstract class EntityAIFindEntityNearestPlayerMixin {

    @Shadow @Final private EntityLiving entityLiving;
    @Shadow private EntityLivingBase entityTarget;
    @Shadow @Final private Predicate<Entity> predicate;

    @Shadow protected abstract double maxTargetRange();

    @Inject(method = "shouldExecute", at = @At("HEAD"), cancellable = true)
    private void findClosestPlayer(CallbackInfoReturnable<Boolean> cir) {
        EntityLiving origin = this.entityLiving;
        List<EntityPlayer> players = origin.world.playerEntities;

        if (players.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        double maxRange = this.maxTargetRange();
        double maxRangeSq = maxRange * maxRange;

        AxisAlignedBB originBB = origin.getEntityBoundingBox();

        double maxYLimit = originBB.maxY + 4.0;
        double minYLimit = originBB.minY - 4.0;

        EntityPlayer closestPlayer = null;
        double minDistanceSq = maxRangeSq;

        for (EntityPlayer player : players) {
            AxisAlignedBB playerBB = player.getEntityBoundingBox();
            if (playerBB.minY > maxYLimit || playerBB.maxY < minYLimit) {
                continue;
            }

            double distSq = origin.getDistanceSq(player);

            if (distSq >= minDistanceSq) {
                continue;
            }

            if (this.predicate.apply(player)) {
                minDistanceSq = distSq;
                closestPlayer = player;
            }
        }

        this.entityTarget = closestPlayer;
        cir.setReturnValue(closestPlayer != null);
    }
}