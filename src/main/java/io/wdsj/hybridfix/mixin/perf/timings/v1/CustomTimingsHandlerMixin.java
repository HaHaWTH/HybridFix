package io.wdsj.hybridfix.mixin.perf.timings.v1;

import org.spigotmc.CustomTimingsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintStream;
import java.util.Queue;

@Mixin(CustomTimingsHandler.class)
public abstract class CustomTimingsHandlerMixin {
    @Redirect(
            method = "<init>(Ljava/lang/String;Lorg/spigotmc/CustomTimingsHandler;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"
            ),
            remap = false
    )
    private boolean init(Queue<CustomTimingsHandler> instance, Object e) {
        // no-op
        return false;
    }

    @Inject(
            method = "printTimings",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void printTimings(PrintStream printStream, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "reload",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void reload(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void tick(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "startTiming",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private void startTiming(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "stopTiming",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private void stopTiming(CallbackInfo ci) {
        ci.cancel();
    }
}
