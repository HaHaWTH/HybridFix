package io.wdsj.hybridfix.mixin.fix.fakeplayer;

import io.wdsj.hybridfix.config.Settings;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
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
    @Shadow
    @Final
    private Plugin plugin;
    @Unique
    private static final Set<String> hybridFix$blacklist = new ObjectOpenHashSet<>(Arrays.asList(Settings.fakePlayerPluginBlacklist));

    @Inject(
            method = "callEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    public void ignoreFakePlayer(Event event, CallbackInfo ci) {
        if (event instanceof BlockBreakEvent) {
            hybridFix$handleBlockBreakEvent((BlockBreakEvent) event, plugin, ci);
            return;
        }
        if (event instanceof EntityChangeBlockEvent) {
            hybridFix$handleEntityChangeBlockEvent((EntityChangeBlockEvent) event, plugin, ci);
        }
    }

    @Unique
    private static void hybridFix$handleBlockBreakEvent(BlockBreakEvent event, Plugin plugin, CallbackInfo ci) {
        EntityPlayerMP player = ((CraftPlayer) event.getPlayer()).getHandle();
        if (player instanceof FakePlayer && hybridFix$isListedPlugin(plugin.getName())) {
            ci.cancel();
        }
    }

    @Unique
    private static void hybridFix$handleEntityChangeBlockEvent(EntityChangeBlockEvent event, Plugin plugin, CallbackInfo ci) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        EntityPlayerMP player = ((CraftPlayer) entity).getHandle();
        if (player instanceof FakePlayer && hybridFix$isListedPlugin(plugin.getName())) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean hybridFix$isListedPlugin(String pluginName) {
        return Settings.invertFakePlayerBlacklist != hybridFix$blacklist.contains(pluginName);
    }
}
