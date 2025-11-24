package io.wdsj.hybridfix;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.metric.Metrics;
import io.wdsj.hybridfix.handler.BukkitForgePermissionHandler;
import io.wdsj.hybridfix.handler.explosion.ExplosionDetonateHandler;
import io.wdsj.hybridfix.handler.LivingAttackHandler;
import io.wdsj.hybridfix.handler.explosion.ExplosionStartHandler;
import io.wdsj.hybridfix.util.Updater;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.PermissionAPI;
import org.bukkit.Bukkit;

import static io.wdsj.hybridfix.HybridFix.IS_CLEANROOM;

public class HybridFixServer {
    public static void preInit() {
        if (!IS_CLEANROOM) {
            if (Settings.passExplosionEventToBukkit) {
                MinecraftForge.EVENT_BUS.register(new ExplosionDetonateHandler());
            }
            if (Settings.passExplosionStartEventToBukkit) {
                MinecraftForge.EVENT_BUS.register(new ExplosionStartHandler());
            }
            if (Settings.bridgeForgePermissionsToBukkit) {
                PermissionAPI.setPermissionHandler(new BukkitForgePermissionHandler());
            }
        }
        MinecraftForge.EVENT_BUS.register(new LivingAttackHandler());
    }

    public static void onStartComplete() {
        if (Settings.enableMetrics) {
            new Metrics(HybridFixInternalPlugin.getInstance(), 24273);
        }
        if (Updater.isDev()) {
            HybridFix.LOGGER.info("You are running a development build of HybridFix, please report any bugs to https://github.com/HaHaWTH/HybridFix/issues");
        }
        if (Settings.checkForUpdates) {
            Utils.commonWorker().submit(() -> {
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

    public static void createServerDump(Throwable t) {
        HybridFix.LOGGER.error("--- REPORT THIS TO YOUR SERVER SOFTWARE - If you are sure this is a HybridFix bug, please report it at https://github.com/HaHaWTH/HybridFix/issues - {} ---", Bukkit.getServer().getVersion());
        HybridFix.LOGGER.error(t);
    }
}
