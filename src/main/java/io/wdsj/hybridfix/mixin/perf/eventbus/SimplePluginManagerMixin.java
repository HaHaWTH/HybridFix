package io.wdsj.hybridfix.mixin.perf.eventbus;

import org.bukkit.event.Event;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimplePluginManager.class)
public abstract class SimplePluginManagerMixin {
    @Inject(
            method = "callEvent",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void skipEventIfNoListeners(Event event, CallbackInfo ci) {
        if (event.getHandlers().getRegisteredListeners().length == 0) {
            ci.cancel();
        }
    }
}
