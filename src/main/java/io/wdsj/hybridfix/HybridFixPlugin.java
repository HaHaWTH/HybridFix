package io.wdsj.hybridfix;

import com.google.common.collect.ImmutableMap;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.Utils;
import io.wdsj.hybridfix.util.reflection.FluentReflect;
import io.wdsj.hybridfix.util.reflection.UnsafeFieldAccessor;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.wdsj.hybridfix.HybridFix.*;

@IFMLLoadingPlugin.Name("HybridFixPlugin")
public class HybridFixPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final boolean isClient = FMLLaunchHandler.side().isClient();

    private static final Map<String, Supplier<Boolean>> serversideMixinConfigs = ImmutableMap.copyOf(new LinkedHashMap<String, Supplier<Boolean>>()
    {
        {
            if (IS_HYBRID_ENV) {
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
                put("mixins.fix.error_recovery.json", () -> Settings.errorRecoverySettings.enable);
                put("mixins.fix.forge.ping_status.json", () -> Settings.fixOutdatedServerPingStatus);
            }
        }
    });

    private static final Map<String, Supplier<Boolean>> commonMixinConfigs = ImmutableMap.copyOf(new LinkedHashMap<String, Supplier<Boolean>>()
    {
        {
            put("mixins.hybridfix.base.patch.forge.universal.json", () -> true);
            put("mixins.perf.ai.universal.json", () -> Settings.optimizeEntityAI);
            put("mixins.perf.core.math.json", () -> Settings.optimizeVec3iHashing);
        }
    });

    private final Map<String, Supplier<Boolean>> extraMixinConfigs = new LinkedHashMap<>();

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        if (!isClient) configs.addAll(serversideMixinConfigs.keySet());
        configs.addAll(commonMixinConfigs.keySet());
        configs.addAll(extraMixinConfigs.keySet());
        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        Supplier<Boolean> sidedSupplier = isClient ? null : serversideMixinConfigs.get(mixinConfig);
        Supplier<Boolean> commonSupplier = commonMixinConfigs.get(mixinConfig);
        Supplier<Boolean> extraSupplier = extraMixinConfigs.get(mixinConfig);
        if (sidedSupplier != null) {
            return sidedSupplier.get();
        }
        if (commonSupplier != null) {
            return commonSupplier.get();
        }
        if (extraSupplier != null) {
            return extraSupplier.get();
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
        if (!initialized) this.initModules();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    private volatile boolean initialized = false;
    synchronized void initModules() {
        if (initialized) return;
        if (Settings.modPatchSettings.patchQuarkASM && Utils.isClassExists("vazkii.quark.base.asm.ClassTransformer")) {
            try {
                UnsafeFieldAccessor transformers = FluentReflect.fromClass("vazkii.quark.base.asm.ClassTransformer")
                        .name("transformers")
                        .staticFieldAccessor();
                Map<String, Object> transformersMap = transformers.get(null);
                transformersMap.remove("net.minecraft.entity.Entity");
                extraMixinConfigs.put("mixins.quark.asm_fix.json", () -> true);
                LOGGER.info("Replaced Quark's EntityTransformer with our own mixins");
            } catch (Throwable t) {
                LOGGER.error("Failed to patch Quark ASM", t);
            }
        }
        initialized = true;
    }
}
