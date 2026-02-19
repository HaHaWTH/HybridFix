package io.wdsj.hybridfix.mixin.api.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.map.MapView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = Bukkit.class, remap = false)
public abstract class BukkitMixin {
    @Shadow private static Server server;

    @Unique(silent = true)
    public MapView getMap(int id) {
        return server.getMap((short) id);
    }
}
