package io.wdsj.hybridfix.mixin.asm_fix.quark;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import io.wdsj.hybridfix.util.reflection.FluentReflect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;

/**
 * Based on {@link vazkii.quark.base.asm.ClassTransformer#transformEntity(byte[])}
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private static final MethodHandle hybridFix$recordMotionHandle = FluentReflect.fromClass("vazkii.quark.base.asm.ASMHooks")
            .name("recordMotion")
            .params(Entity.class)
            .accessible(true)
            .staticMethodHandle();
    @Unique
    private static final MethodHandle hybridFix$applyCollisionLogicHandle = FluentReflect.fromClass("vazkii.quark.base.asm.ASMHooks")
            .name("applyCollisionLogic")
            .params(Entity.class, double.class, double.class, double.class, double.class, double.class, double.class)
            .accessible(true)
            .staticMethodHandle();
    @Unique
    private static final MethodHandle hybridFix$onEntityUpdateHandle = FluentReflect.fromClass("vazkii.quark.base.asm.ASMHooks")
            .name("onEntityUpdate")
            .params(Entity.class)
            .accessible(true)
            .staticMethodHandle();
    @Inject(method = "move", at = @At("HEAD"))
    private void onMoveHead(MoverType type, double x, double y, double z, CallbackInfo ci, @Share("originX") LocalDoubleRef originX, @Share("originY") LocalDoubleRef originY, @Share("originZ") LocalDoubleRef originZ) throws Throwable {
        originX.set(x);
        originY.set(y);
        originZ.set(z);
        //ASMHooks.recordMotion((Entity) (Object) this);
        hybridFix$recordMotionHandle.invokeExact((Entity) (Object) this);
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isWet()Z"
            ),
            allow = 1
    )
    private void quark$applyCollisionLogic(
            MoverType type, double x, double y, double z, CallbackInfo ci, @Share("originX") LocalDoubleRef originX, @Share("originY") LocalDoubleRef originY, @Share("originZ") LocalDoubleRef originZ) throws Throwable {
        //ASMHooks.applyCollisionLogic((Entity) (Object) this, originX.get(), originY.get(), originZ.get(), x, y, z);
        hybridFix$applyCollisionLogicHandle.invokeExact((Entity) (Object) this, originX.get(), originY.get(), originZ.get(), x, y, z);
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    private void onEntityUpdate(CallbackInfo ci) throws Throwable {
        //ASMHooks.onEntityUpdate((Entity) (Object) this);
        hybridFix$onEntityUpdateHandle.invokeExact((Entity) (Object) this);
    }
}
