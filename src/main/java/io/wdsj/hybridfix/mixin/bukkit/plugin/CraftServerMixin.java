package io.wdsj.hybridfix.mixin.bukkit.plugin;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.event.HandlerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftServer.class)
public abstract class CraftServerMixin {

    @Inject(
            method = "disablePlugins",
            at = @At(
                    value = "TAIL"
            ),
            remap = false
    )
    public void onDisablePlugins(CallbackInfo ci) {
        hybridFix$disableInternalPlugin();
    }

    @Unique
    private void hybridFix$disableInternalPlugin() {
        HybridFix.LOGGER.info("[HybridFix] Disabling HybridFix internal plugin v{}", HybridFix.VERSION);
        HandlerList.unregisterAll(HybridFixInternalPlugin.getInstance());
    }
}
