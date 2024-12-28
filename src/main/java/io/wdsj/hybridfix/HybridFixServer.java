package io.wdsj.hybridfix;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.handler.BukkitForgePermissionHandler;
import io.wdsj.hybridfix.handler.ExplosionHandler;
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
}
