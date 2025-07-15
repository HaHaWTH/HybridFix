package io.wdsj.hybridfix.mixin.bridge.forge_bukkit;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.util.ListenerHackery;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(
            method = "loadAllWorlds",
            at = @At(
                    value = "TAIL"
            )
    )
    public void onLoadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions, CallbackInfo ci) throws IllegalAccessException {
        if (ListenerHackery.childLoadingEnabled != null) {
            ListenerHackery.childLoadingEnabled.setBoolean(null, true);
            HybridFix.LOGGER.info("Forge to Bukkit access now enabled.");
        }
    }
}