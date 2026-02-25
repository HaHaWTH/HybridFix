package io.wdsj.hybridfix.mixin.bukkit.plugin;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.hook.citizens.CitizensHookNPCDamageListener;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.ResHookAE2SpatialPylonListener;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.ResHookBlockFormListener;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.ResHookEntityChangeBlockListener;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.config_editor.ResidenceCustomBlockAdder;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.voxel_map.VoxelMapResidenceDataSender;
import io.wdsj.hybridfix.entry.bukkit.hook.worldguard.WGHookAE2SpatialPylonListener;
import io.wdsj.hybridfix.entry.bukkit.hook.worldguard.WGHookBlockFormListener;
import io.wdsj.hybridfix.entry.bukkit.hook.worldguard.WGHookEntityChangeBlockListener;
import io.wdsj.hybridfix.entry.bukkit.hook.worldguard.WGHookPvpListener;
import io.wdsj.hybridfix.entry.bukkit.listener.ExplodeListener;
import io.wdsj.hybridfix.entry.bukkit.util.ListenerHackery;
import io.wdsj.hybridfix.handler.voxelmap.VMResidenceChannel;
import io.wdsj.hybridfix.util.reflection.FluentReflect;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.Loader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(
            method = "init",
            at = @At(
                    value = "TAIL"
            )
    )
    public void onInit(CallbackInfoReturnable<Boolean> cir) {
        hybridFix$onEnable();
    }

    @Unique
    private void hybridFix$onEnable() {
        HybridFix.LOGGER.info("[HybridFix] Enabling HybridFix internal plugin v{}", HybridFix.VERSION);
        Plugin internalPlugin = HybridFixInternalPlugin.getInstance();
        if (Settings.bukkitPluginConfig.antiExplode) {
            Bukkit.getPluginManager().registerEvents(new ExplodeListener(), internalPlugin);
        }
        if (Settings.bukkitPluginConfig.hookResidence) {
            final String res = "Residence";
            if (Bukkit.getPluginManager().isPluginEnabled(res)) {
                ListenerHackery.registerListenerToTargetPlugin(ResHookEntityChangeBlockListener.class, res);
                ListenerHackery.registerListenerToTargetPlugin(ResHookBlockFormListener.class, res);
                if (Settings.bukkitPluginConfig.sendClientResidenceData) {
                    Bukkit.getMessenger().registerOutgoingPluginChannel(internalPlugin, VMResidenceChannel.CHANNEL);
                    ListenerHackery.registerListenerToTargetPlugin(VoxelMapResidenceDataSender.class, res);
                }
                if (Settings.bukkitPluginConfig.autoAddModBlocksToResidenceConfig) {
                    Plugin residence = Bukkit.getPluginManager().getPlugin(res);
                    ResidenceCustomBlockAdder adder = new ResidenceCustomBlockAdder(residence);
                    adder.addCustomBothClicks();
                    adder.addCustomRightClicks();
                    adder.save();
                    ClassLoader cl = residence.getClass().getClassLoader();
                    FluentReflect.fromClass(residence.getClass(), cl)
                            .name("getConfigManager")
                            .returnType("com.bekvon.bukkit.residence.ConfigManager")
                            .findVirtualMethodHandle()
                            .map(handle -> handle.invokeWithArguments(residence))
                            .accept(configManager -> {
                                FluentReflect.fromClass(configManager.getClass(), cl)
                                        .name("UpdateConfigFile")
                                        .returnType(void.class)
                                        .virtualMethodHandle()
                                        .invokeWithArguments(configManager);
                                HybridFix.LOGGER.info("Reloaded residence configuration.");
                            }, t -> {
                                HybridFix.LOGGER.warn("Unable to reload residence configuration, attempting to use command...", t);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "residence reload config");
                            });
                }
                if (Settings.modPatchSettings.patchAppliedEnergistics2SpatialPylon && Loader.isModLoaded("appliedenergistics2")) {
                    ListenerHackery.registerListenerToTargetPlugin(ResHookAE2SpatialPylonListener.class, res);
                    HybridFix.LOGGER.info("{}[HybridFix] Residence <-> Applied Energistics 2 communication established.", ChatColor.GREEN);
                }
                HybridFix.LOGGER.info("{}[HybridFix] Hooked into Residence.", ChatColor.AQUA);
            } else {
                HybridFix.LOGGER.warn("[HybridFix] Residence not found, check your installation.");
            }
        }
        if (Settings.bukkitPluginConfig.hookWorldGuard) {
            final String wg = "WorldGuard";
            if (Bukkit.getPluginManager().isPluginEnabled(wg)) {
                ListenerHackery.registerListenerToTargetPlugin(WGHookEntityChangeBlockListener.class, wg);
                ListenerHackery.registerListenerToTargetPlugin(WGHookBlockFormListener.class, wg);
                ListenerHackery.registerListenerToTargetPlugin(WGHookPvpListener.class, wg);
                if (Settings.modPatchSettings.patchAppliedEnergistics2SpatialPylon && Loader.isModLoaded("appliedenergistics2")) {
                    ListenerHackery.registerListenerToTargetPlugin(WGHookAE2SpatialPylonListener.class, wg);
                    HybridFix.LOGGER.info("{}[HybridFix] WorldGuard <-> Applied Energistics 2 communication established.", ChatColor.GREEN);
                }
                HybridFix.LOGGER.info("{}[HybridFix] Hooked into WorldGuard.", ChatColor.LIGHT_PURPLE);
            } else {
                HybridFix.LOGGER.warn("[HybridFix] WorldGuard not found, check your installation.");
            }
        }
        if (Settings.bukkitPluginConfig.hookCitizens) {
            final String citizens = "Citizens";
            if (Bukkit.getPluginManager().isPluginEnabled(citizens)) {
                ListenerHackery.registerListenerToTargetPlugin(CitizensHookNPCDamageListener.class, citizens);
                HybridFix.LOGGER.info("{}[HybridFix] Hooked into Citizens.", ChatColor.GOLD);
            } else {
                HybridFix.LOGGER.warn("[HybridFix] Citizens not found, check your installation.");
            }
        }
    }
}
