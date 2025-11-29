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
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.server.permission.PermissionAPI;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class HybridFixServer {
    public static final Set<Class<? extends Entity>> modEntitiesWithoutInactiveTick = new ReferenceOpenHashSet<>();
    public static void preInit() {
        if (Settings.passExplosionEventToBukkit) {
            MinecraftForge.EVENT_BUS.register(new ExplosionDetonateHandler());
        }
        if (Settings.bridgeForgePermissionsToBukkit) {
            PermissionAPI.setPermissionHandler(new BukkitForgePermissionHandler());
        }
        if (Settings.passExplosionStartEventToBukkit) {
            MinecraftForge.EVENT_BUS.register(new ExplosionStartHandler());
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
                Updater.UpdateResult updateResult = Updater.checkNow();
                if (updateResult.isUpdateAvailable()) {
                    HybridFix.LOGGER.warn("There is a new version of HybridFix available: {}, you're on: {}", updateResult.getLatestVersion(), HybridFix.VERSION);
                } else {
                    if (!updateResult.isError()) {
                        HybridFix.LOGGER.info("You are running the latest version.");
                    } else {
                        HybridFix.LOGGER.info("Unable to fetch version info.");
                    }
                }
            });
        }
    }

    public static void onServerAboutToStart() {
        if (Settings.fixEntityActivationRange) {
            for (Map.Entry<ResourceLocation, EntityEntry> entry : ForgeRegistries.ENTITIES.getEntries()) {
                if (entry.getKey().getNamespace().equals("minecraft")) continue;
                try {
                    Class<? extends Entity> clazz = entry.getValue().getEntityClass();
                    Method method = clazz.getMethod("inactiveTick");
                    if (method.getDeclaringClass() != clazz) {
                        modEntitiesWithoutInactiveTick.add(clazz);
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static void createServerDump(Throwable t) {
        HybridFix.LOGGER.error("--- REPORT THIS TO YOUR SERVER SOFTWARE - If you are sure this is a HybridFix bug, please report it at https://github.com/HaHaWTH/HybridFix/issues - {} ---", Bukkit.getServer().getVersion());
        HybridFix.LOGGER.error(t);
    }
}
