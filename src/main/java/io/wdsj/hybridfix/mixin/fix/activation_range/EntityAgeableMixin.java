package io.wdsj.hybridfix.mixin.fix.activation_range;

import io.wdsj.hybridfix.duck.fix.activation_range.EntityEARAccessor;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityAgeable.class)
public abstract class EntityAgeableMixin extends EntityCreature {

    public EntityAgeableMixin(World worldIn) {
        super(worldIn);
    }

    @Dynamic("Spigot EAR")
    @Inject(method = "inactiveTick",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityCreature;inactiveTick()V", shift = At.Shift.AFTER),
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;inactiveTick()V", shift = At.Shift.AFTER),
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;inactiveTick()V", shift = At.Shift.AFTER),
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;inactiveTick()V", shift = At.Shift.AFTER)
            },
            cancellable = true,
            remap = false,
            require = 1
    )
    public void onPostSuperInactiveTick(CallbackInfo ci) {
        if (((EntityEARAccessor) this).hybridFix$isIgnoringEAR()) {
            ci.cancel();
        }
    }
}