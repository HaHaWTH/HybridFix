package io.wdsj.hybridfix;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.metric.Metrics;
import io.wdsj.hybridfix.handler.BukkitForgePermissionHandler;
import io.wdsj.hybridfix.handler.ExplosionHandler;
import io.wdsj.hybridfix.util.Updater;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.PermissionAPI;

public class HybridFixServer {
    public static void preInit() {
        if (Settings.passExplosionEventToBukkit) {
            MinecraftForge.EVENT_BUS.register(new ExplosionHandler());
        }
        if (Settings.bridgeForgePermissionsToBukkit) {
            PermissionAPI.setPermissionHandler(new BukkitForgePermissionHandler());
        }
    }

    public static void onStartComplete() {
        if (Settings.enableMetrics) {
            Metrics metrics = new Metrics(HybridFixInternalPlugin.getInstance(), 24273);
        }
        if (Settings.checkForUpdates) {
            Utils.ioWorker().submit(() -> {
                HybridFix.LOGGER.info("Checking for updates...");
                if (Updater.isUpdateAvailable()) {
                    HybridFix.LOGGER.warn("There is a new version of HybridFix available: {}, you're on: {}", Updater.getLatestVersion(), Updater.getCurrentVersion());
                } else {
                    if (!Updater.isErred()) {
                        HybridFix.LOGGER.info("You are running the latest version.");
                    } else {
                        HybridFix.LOGGER.info("Unable to fetch version info.");
                    }
                }
            });
        }
    }
}
