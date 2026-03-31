package io.wdsj.hybridfix.entry.bukkit.hook.residence.config_editor;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.reflection.HybridReflectionUtils;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class ResidenceCustomBlockAdder {
    private final Set<String> addedMaterials = new HashSet<>();
    private final Plugin plugin;

    private static final String customRightClickKey = System.getProperty("hybridfix.hook.residence.customRightClickKey", "Global.CustomRightClick");
    private static final String customBothClickKey = System.getProperty("hybridfix.hook.residence.customBothClickKey", "Global.CustomBothClick");
    private static final boolean purgeUnavailableMaterials = Boolean.getBoolean("hybridfix.hook.residence.purgeUnavailableMaterials");

    public ResidenceCustomBlockAdder(Plugin plugin) {
        this.plugin = plugin;
        List<String> oldRightClicks = plugin.getConfig().getStringList(customRightClickKey);
        addedMaterials.addAll(oldRightClicks);
        List<String> oldBothClicks = plugin.getConfig().getStringList(customBothClickKey);
        addedMaterials.addAll(oldBothClicks);
    }
    public void addCustomRightClicks() {
        final long start = System.currentTimeMillis();
        List<String> oldRightClicks = plugin.getConfig().getStringList(customRightClickKey);
        if (purgeUnavailableMaterials) {
            List<String> purgedList = oldRightClicks.stream()
                    .distinct()
                    .filter(m -> Material.getMaterial(m) != null)
                    .collect(Collectors.toList());
            int purged = oldRightClicks.size() - purgedList.size();
            if (purged > 0) {
                HybridFix.LOGGER.info("Purged {} unavailable materials from Residence custom right clicks", purged);
            }
            oldRightClicks = purgedList;
        }
        List<String> newRightClicks = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation key = entry.getKey();
            Block block = entry.getValue();
            if (key.getNamespace().equals("minecraft")) continue;
            String materialName = key.toString().toUpperCase().replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
            try {
                Material material = Material.getMaterial(materialName);
                if (material != null && HybridReflectionUtils.isMethodOverriddenByModSafe(block.getClass(), "func_180639_a", "onBlockActivated") && !addedMaterials.contains(materialName)) {
                    newRightClicks.add(materialName);
                    addedMaterials.add(materialName);
                }
            } catch (Throwable e)  {
                HybridFix.LOGGER.warn("Failed to add custom right click for block {}", materialName, e);
            }
        }
        newRightClicks.addAll(oldRightClicks);
        plugin.getConfig().set(customRightClickKey, newRightClicks);
        final long end = System.currentTimeMillis();
        HybridFix.LOGGER.info("Add {} custom right click(s) to Residence, {} in total. (took {} ms)", newRightClicks.size() - oldRightClicks.size(), newRightClicks.size(), end - start);
    }

    public void addCustomBothClicks() {
        final long start = System.currentTimeMillis();
        List<String> oldBothClicks = plugin.getConfig().getStringList(customBothClickKey);
        if (purgeUnavailableMaterials) {
            List<String> purgedList = oldBothClicks.stream()
                    .distinct()
                    .filter(m -> Material.getMaterial(m) != null)
                    .collect(Collectors.toList());
            int purged = oldBothClicks.size() - purgedList.size();
            if (purged > 0) {
                HybridFix.LOGGER.info("Purged {} unavailable materials from Residence custom both clicks", purged);
            }
            oldBothClicks = purgedList;
        }
        List<String> newBothClicks = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation key = entry.getKey();
            Block block = entry.getValue();
            if (key.getNamespace().equals("minecraft")) continue;
            String materialName = key.toString().toUpperCase().replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
            try {
                Material material = Material.getMaterial(materialName);
                if (material != null && HybridReflectionUtils.isMethodOverriddenByModSafe(block.getClass(), "func_180639_a", "onBlockActivated") && HybridReflectionUtils.isMethodOverriddenByModSafe(block.getClass(), "func_180649_a", "onBlockClicked") && !addedMaterials.contains(materialName)) {
                    newBothClicks.add(materialName);
                    addedMaterials.add(materialName);
                }
            } catch (Throwable t)  {
                HybridFix.LOGGER.warn("Failed to add custom both click for block {}", materialName, t);
            }
        }
        newBothClicks.addAll(oldBothClicks);
        plugin.getConfig().set(customBothClickKey, newBothClicks);
        final long end = System.currentTimeMillis();
        HybridFix.LOGGER.info("Add {} custom both click(s) to Residence, {} in total. (took {} ms)", newBothClicks.size() - oldBothClicks.size(), newBothClicks.size(), end - start);
    }

    public void save() {
        plugin.saveConfig();
    }
}
