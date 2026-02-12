package io.wdsj.hybridfix.mixin.late.aether_legacy.universal.feature.entity.swet_dissolve;

import com.gildedgames.the_aether.entities.passive.mountable.EntitySwet;
import com.gildedgames.the_aether.entities.util.EntityMountable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySwet.class)
public abstract class EntitySwetMixin extends EntityMountable {
    @Shadow(remap = false) public float swetWidth;
    @Shadow(remap = false) public float swetHeight;

    public EntitySwetMixin(World world) {
        super(world);
    }
    @Unique private int hybridFix$dissolveTimer = 0;
    @Unique private static final int MAX_DISSOLVE_TIME = 80;

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void saveDissolveState(NBTTagCompound compound, CallbackInfo ci) {
        compound.setInteger("HybridFix.DissolveTimer", this.hybridFix$dissolveTimer);
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void loadDissolveState(NBTTagCompound compound, CallbackInfo ci) {
        if (compound.hasKey("HybridFix.DissolveTimer")) {
            this.hybridFix$dissolveTimer = compound.getInteger("DissolveTimer");
        }
    }

    @WrapOperation(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gildedgames/the_aether/entities/passive/mountable/EntitySwet;handleWaterMovement()Z"
            )
    )
    private boolean alsoDissolveInRain(EntitySwet instance, Operation<Boolean> original) {
        return original.call(instance) || this.isWet();
    }

    @WrapOperation(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gildedgames/the_aether/entities/passive/mountable/EntitySwet;dissolveSwet()V",
                    remap = false
            )
    )
    private void interceptDeath(EntitySwet instance, Operation<Void> original) {
        this.hybridFix$dissolveTimer++;

        if (this.isBeingRidden()) {
            this.removePassengers();
        }

        if (this.hybridFix$dissolveTimer >= MAX_DISSOLVE_TIME) {
            original.call(instance);
        }
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void applyShrinking(CallbackInfo ci) {
        if (this.hybridFix$dissolveTimer <= 0 || this.isDead) return;

        float progress = (float) this.hybridFix$dissolveTimer / MAX_DISSOLVE_TIME;
        float scale = Math.max(0.25F, 1.0F - progress);

        this.setSize(0.8F * scale, 0.8F * scale);
        this.swetWidth *= scale;
        this.swetHeight *= scale;
    }

    @Inject(
            method = "capturePrey",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void preventMountingWhenShrinking(EntityPlayer entity, CallbackInfo ci) {
        if (this.hybridFix$dissolveTimer > 0) {
            ci.cancel();
        }
    }
}
