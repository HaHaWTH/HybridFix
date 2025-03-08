package io.wdsj.hybridfix.mixin.base.patch.craftbukkit;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.SpigotReflectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Set;

@Mixin(value = RegisteredListener.class, remap = false)
public abstract class RegisteredListenerMixin {
    @Shadow @Final private Plugin plugin;
    @Unique
    private static final Set<String> hybridFix$blacklist = new ObjectOpenHashSet<>(Arrays.asList(Settings.fakePlayerPluginBlacklist));

    @Inject(
            method = "callEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    public void ignoreFakePlayer(Event event, CallbackInfo ci) {
        if (Settings.fakePlayerPluginBlacklist.length == 0) return;
        if (event instanceof BlockBreakEvent) {
            EntityPlayerMP player = SpigotReflectionUtils.CraftPlayer_getHandle((CraftPlayer) ((BlockBreakEvent) event).getPlayer());
            if (player instanceof FakePlayer && hybridFix$isListedPlugin(plugin.getName())) {
                ci.cancel();
            }
        }
    }

    @Unique
    private static boolean hybridFix$isListedPlugin(String pluginName) {
        return Settings.invertFakePlayerBlacklist != hybridFix$blacklist.contains(pluginName);
    }
}
