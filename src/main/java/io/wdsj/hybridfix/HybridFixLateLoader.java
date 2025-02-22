package io.wdsj.hybridfix;

import com.google.common.collect.ImmutableMap;
import io.wdsj.hybridfix.config.Settings;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.wdsj.hybridfix.HybridFix.IS_HYBRID_ENV;

@SuppressWarnings("unused")
public class HybridFixLateLoader implements ILateMixinLoader {
    public static final boolean isClient = FMLLaunchHandler.side().isClient();

    private static final Map<String, Supplier<Boolean>> serversideMixinConfigs = ImmutableMap.copyOf(new LinkedHashMap<String, Supplier<Boolean>>()
    {
        {
            // Twilight Forest patches
            put("mixins.twilight_forest.sapling.json", () -> isModLoaded("twilightforest") && Settings.modPatchSettings.patchTwilightForestSapling);
            put("mixins.twilight_forest.item.json", () -> isModLoaded("twilightforest") && Settings.modPatchSettings.patchTwilightForestItem);
            put("mixins.twilight_forest.entity.json", () -> isModLoaded("twilightforest") && Settings.modPatchSettings.patchTwilightForestEntityEvent);
            // Thaumcraft patches
            put("mixins.thaumcraft.taint.json", () -> isModLoaded("thaumcraft") && Settings.modPatchSettings.patchThaumcraftTaintSpread);
            // Tconstruct patches
            put("mixins.tconstruct.tools.json", () -> isModLoaded("tconstruct") && Settings.modPatchSettings.patchTconstructToolDamage);
        }
    });

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        if (!IS_HYBRID_ENV) return configs;
        if (!isClient) configs.addAll(serversideMixinConfigs.keySet());
        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if (!IS_HYBRID_ENV) return false;
        Supplier<Boolean> sidedSupplier = isClient ? null : serversideMixinConfigs.get(mixinConfig);
        if (sidedSupplier != null) {
            return sidedSupplier.get();
        }
        return true;
    }

    private static boolean isModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }
}
