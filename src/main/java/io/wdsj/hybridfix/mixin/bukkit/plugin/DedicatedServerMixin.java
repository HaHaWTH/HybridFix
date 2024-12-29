package io.wdsj.hybridfix.mixin.bukkit.plugin;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.listener.ExplodeListener;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(
            method = "init",
            at = @At(
                    value = "TAIL"
            )
    )
    public void onInit(CallbackInfoReturnable<Boolean> cir) {
        hybridFix$register();
    }

    @Unique
    private void hybridFix$register() {
        HybridFix.LOGGER.info("[HybridFix] Enabling HybridFix internal plugin v{}", HybridFix.VERSION);
        if (Settings.bukkitPluginConfig.antiExplode) {
            Bukkit.getServer().getPluginManager().registerEvents(new ExplodeListener(), HybridFixInternalPlugin.getInstance());
        }
    }
}
