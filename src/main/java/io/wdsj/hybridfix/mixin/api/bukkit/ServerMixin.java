package io.wdsj.hybridfix.mixin.api.bukkit;

import org.bukkit.Server;
import org.bukkit.map.MapView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = Server.class, remap = false)
public interface ServerMixin {
    @Unique(silent = true)
    MapView getMap(int id);
}
