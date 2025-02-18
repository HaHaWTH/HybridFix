package io.wdsj.hybridfix.mixin.bukkit.plugin;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.ResHookBlockFormListener;
import io.wdsj.hybridfix.entry.bukkit.listener.ExplodeListener;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
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
        hybridFix$onEnable();
    }

    @Unique
    private void hybridFix$onEnable() {
        HybridFix.LOGGER.info("[HybridFix] Enabling HybridFix internal plugin v{}", HybridFix.VERSION);
        Plugin internalPlugin = HybridFixInternalPlugin.getInstance();
        if (Settings.bukkitPluginConfig.antiExplode) {
            Bukkit.getPluginManager().registerEvents(new ExplodeListener(), internalPlugin);
        }
        if (Settings.bukkitPluginConfig.residenceHook) {
            if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
                Bukkit.getPluginManager().registerEvents(new ResHookBlockFormListener(), internalPlugin);
                HybridFix.LOGGER.info("{}[HybridFix] Hooked into Residence.", ChatColor.AQUA);
            } else {
                HybridFix.LOGGER.warn("[HybridFix] Residence not found, check your installation.");
            }
        }
    }
}
