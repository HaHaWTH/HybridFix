package io.wdsj.hybridfix;

import com.google.common.collect.ImmutableMap;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static io.wdsj.hybridfix.HybridFix.IS_CLEANROOM;
import static io.wdsj.hybridfix.HybridFix.IS_HYBRID_ENV;

@IFMLLoadingPlugin.Name("HybridFixPlugin")
public class HybridFixPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final boolean isClient = FMLLaunchHandler.side().isClient();

    private static final Map<String, Supplier<Boolean>> serversideMixinConfigs = ImmutableMap.copyOf(new LinkedHashMap<String, Supplier<Boolean>>()
    {
        {
            put("mixins.hybridfix.base.json", () -> true);
            put("mixins.bridge.forge_bukkit.json", () -> Settings.forgeModCallBukkitPlugin);
            put("mixins.stacktrace.deobfuscate.json", () -> Settings.deobfuscateStacktrace);
            put("mixins.api.bukkit.json", () -> Settings.extraBukkitApi);
            put("mixins.api.antixray.json", () -> Settings.rayTraceAntiXraySDK);
            put("mixins.fix.fakeplayer.json", () -> Settings.invertFakePlayerBlacklist || Settings.fakePlayerPluginBlacklist.length > 0);
            put("mixins.fix.respawn.json", () -> Settings.fixCapabilityReset);
            put("mixins.fix.chunk_system.json", () -> Settings.fixesForForgeAndBukkitChunkSystems);
            if (!IS_CLEANROOM) {
                if (Utils.isMohist) {
                    put("mixins.fix.respawn.mohist.json", () -> Settings.fixCapabilityReset);
                }
                if (Utils.isMohist) {
                    put("mixins.bridge.explosion.mohist.json", () -> Settings.passExplosionEventToBukkit && Settings.overrideMohistExplosionHandling);
                }
                put("mixins.perf.eventbus.json", () -> Settings.skipEventIfNoListeners);
                if (!Utils.isMohist) {
                    put("mixins.perf.timings.v1.json", () -> Settings.disableTimings);
                }
                put("mixins.perf.server.json", () -> Settings.enableCraftServerOptimizations);
            }
            put("mixins.misc.command.json", () -> Settings.registerHybridFixCommands);
            put("mixins.bukkit.plugin.json", () -> Settings.bukkitPluginConfig.enable);
            put("mixins.fix.packet_limiter.drop_item.json", () -> Settings.packetSettings.maxDroppedItemsPerTick != 20);
            put("mixins.debug.health.json", () -> Settings.debugSettings.entityHealthDebugger);
            put("mixins.perf.te.snapshot.json", () -> Settings.dontCreateTESnapshotForInventoryMoveItemEvent && Settings.extraBukkitApi);
            put("mixins.asm.plugin_patcher.json", () -> Settings.pluginPatcherSettings.enable);
            put("mixins.fix.activation_range.json", () -> Settings.fixEntityActivationRange);
        }
    });

    private static final Map<String, Supplier<Boolean>> commonMixinConfigs = ImmutableMap.copyOf(new LinkedHashMap<String, Supplier<Boolean>>()
    {
    });

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        if (!IS_HYBRID_ENV && !isClient) return configs;
        if (!isClient) configs.addAll(serversideMixinConfigs.keySet());
        configs.addAll(commonMixinConfigs.keySet());
        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if (!IS_HYBRID_ENV && !isClient) return false;
        Supplier<Boolean> sidedSupplier = isClient ? null : serversideMixinConfigs.get(mixinConfig);
        Supplier<Boolean> commonSupplier = commonMixinConfigs.get(mixinConfig);
        if (sidedSupplier != null) {
            return sidedSupplier.get();
        }
        if (commonSupplier != null) {
            return commonSupplier.get();
        }
        return true;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
