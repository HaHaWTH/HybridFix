package io.wdsj.hybridfix.mixin.debug.health;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.HybridFixServer;
import io.wdsj.hybridfix.util.ClassUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    @Unique
    private static final Function<@NotNull Class<?>, Boolean> hybridFix$safetyCheckFunction = clazz -> {
        String name = clazz.getName();
        return name.startsWith("net.minecraft") || name.startsWith("org.bukkit");
    };

    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "setHealth",
            at = @At("HEAD")
    )
    public void debug$setHealth(float health, CallbackInfo ci) {
        try {
            Class<?> caller = ClassUtils.getCallerClass(1); // Skip first frame for mixin generated injectors
            if (caller == null) {
                HybridFix.LOGGER.debug("Unknown caller of EntityLivingBase#setHealth");
                return;
            }
            boolean isSafeAccess = hybridFix$safetyCheckFunction.apply(caller);
            if (!isSafeAccess) {
                HybridFix.LOGGER.warn("Unsafe direct call of EntityLivingBase#setHealth for entity {} with id {} at {} {} detected, caller: {}", this.getName(), this.getEntityId(), this.getEntityWorld().getWorldInfo().getWorldName(), this.getPosition().toString(), caller.getName());
                HybridFix.LOGGER.warn("This will bypass all bukkit events, grief may happen.");
            } else {
                HybridFix.LOGGER.debug("Caller of EntityLivingBase#setHealth: {}", caller.getName());
            }
        } catch (Exception e) {
            HybridFix.LOGGER.error("Error occurred while getting caller of EntityLivingBase#setHealth");
            HybridFixServer.createServerDump(e);
        }
    }
}
