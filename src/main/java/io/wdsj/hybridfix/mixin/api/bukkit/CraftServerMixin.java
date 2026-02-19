package io.wdsj.hybridfix.mixin.api.bukkit;

import io.wdsj.hybridfix.duck.api.bukkit.IServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.map.MapView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = CraftServer.class, remap = false)
public abstract class CraftServerMixin implements IServer {
    @Shadow public abstract MapView getMap(short par1);

    @Override
    public MapView getMap(int id) {
        return this.getMap((short) id);
    }
}
