package io.wdsj.hybridfix;

import com.google.common.collect.ImmutableMap;
import io.wdsj.hybridfix.config.Settings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.wdsj.hybridfix.HybridFix.IS_HYBRID_ENV;
import static io.wdsj.hybridfix.HybridFix.LOGGER;

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
            put("mixins.thaumcraft.flux.json", () -> isModLoaded("thaumcraft") && Settings.modPatchSettings.patchThaumcraftFlux);
            // Tconstruct patches
            put("mixins.tconstruct.tools.json", () -> isModLoaded("tconstruct") && Settings.modPatchSettings.patchTconstructToolDamage);
            // So Many Enchantments patches
            put("mixins.so_many_enchantments.disarm.json", () -> {
                String modId = "somanyenchantments";
                if (isModLoaded(modId) && Settings.modPatchSettings.patchSoManyEnchantmentsDisarm) {
                    String range = "[1.0.0,)";
                    if (isModVersionInRange(modId, range)) {
                        return true;
                    } else {
                        LOGGER.warn("So Many Enchantments version mismatch! Disabling patch. (Expected version is {})", range);
                    }
                }
                return false;
            });
            // Botania patches
            put("mixins.botania.item.json", () -> isModLoaded("botania") && Settings.modPatchSettings.patchBotaniaLens);
            put("mixins.botania.block.json", () -> isModLoaded("botania") && Settings.modPatchSettings.patchBotaniaBlock);
            // Industrial Craft patches
            put("mixins.ic2.machine.json", () -> isModLoaded("ic2") && Settings.modPatchSettings.patchIC2Machine);
            put("mixins.ic2.explosion.json", () -> {
                if (isModLoaded("ic2") && Settings.modPatchSettings.patchIC2Explosion) {
                    ModContainer container = FMLCommonHandler.instance().findContainerFor("ic2");
                    if (container != null) {
                        String currentVersion = container.getVersion();
                        String expectedVersion = "2.8.222-ex112";
                        if (!currentVersion.equals(expectedVersion)) {
                            HybridFix.LOGGER.warn("IC2 version mismatch! Things may not work well. (Expected: {}, you got: {})", expectedVersion, currentVersion);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            });
            // Draconic Evolution patches
            put("mixins.draconic_evolution.entity.json", () -> isModLoaded("draconicevolution") && Settings.modPatchSettings.patchDraconicEvolutionEntity);
            // Reborn Core patches
            put("mixins.reborncore.explosion.json", () -> isModLoaded("reborncore") && Settings.modPatchSettings.patchRebornCoreExplosion);
            // Applied Energistics 2 patches
            put("mixins.applied_energistics_2.spatial.json", () -> isModLoaded("appliedenergistics2") && Settings.modPatchSettings.patchAppliedEnergistics2SpatialPylon);
            // TechGuns patches
            put("mixins.techguns.explosion.json", () -> isModLoaded("techguns") && Settings.modPatchSettings.patchTechGunsExplosion);
            // Infernal Mobs patches
            put("mixins.infernal_mobs.modifiers.json", () -> isModLoaded("infernalmobs") && Settings.modPatchSettings.patchInfernalMobsModifier);
            // Epic Siege Mod patches
            put("mixins.epic_siege_mod.grief.json", () -> isModLoaded("epicsiegemod") && Settings.modPatchSettings.patchEpicSiegeModAi);
            // Industrial Foregoing patches
            put("mixins.industrial_foregoing.block.json", () -> isModLoaded("industrialforegoing") && Settings.modPatchSettings.disableIndustrialForegoingBlackholeControllerRecipe);
            // Witchery patches
            put("mixins.witchery.symbol.json", () -> isModLoaded("witchery") && Settings.modPatchSettings.patchWitcherySymbolEffect);
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

    private static boolean isModVersionInRange(String modId, String versionRange) {
        ModContainer modContainer = FMLCommonHandler.instance().findContainerFor(modId);
        if (modContainer == null) {
            return false;
        }
        String actualVersionString = modContainer.getVersion();
        ArtifactVersion actualVersion = new DefaultArtifactVersion(actualVersionString);

        try {
            VersionRange requiredRange = VersionRange.createFromVersionSpec(versionRange);
            return requiredRange.containsVersion(actualVersion);
        } catch (InvalidVersionSpecificationException e) {
            HybridFix.LOGGER.error("Invalid version range specification: {}", versionRange, e);
            return false;
        }
    }
}
