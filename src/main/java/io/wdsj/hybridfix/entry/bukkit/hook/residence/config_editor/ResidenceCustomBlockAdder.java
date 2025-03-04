package io.wdsj.hybridfix.entry.bukkit.hook.residence.config_editor;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.ObfHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

public class ResidenceCustomBlockAdder {
    private final Set<String> addedMaterials = new HashSet<>();
    private final Plugin plugin;

    private static final String customRightClickKey = System.getProperty("hybridfix.hook.residence.customRightClickKey", "Global.CustomRightClick");
    private static final String customBothClickKey = System.getProperty("hybridfix.hook.residence.customBothClickKey", "Global.CustomBothClick");
    private static final boolean purgeUnavailableMaterials = Boolean.getBoolean("hybridfix.hook.residence.purgeUnavailableMaterials");

    public ResidenceCustomBlockAdder() {
        this.plugin = Bukkit.getPluginManager().getPlugin("Residence");
        List<String> oldRightClicks = plugin.getConfig().getStringList(customRightClickKey);
        addedMaterials.addAll(oldRightClicks);
        List<String> oldBothClicks = plugin.getConfig().getStringList(customBothClickKey);
        addedMaterials.addAll(oldBothClicks);
    }
    public void addCustomRightClicks() {
        final long start = System.currentTimeMillis();
        List<String> oldRightClicks = plugin.getConfig().getStringList(customRightClickKey);
        if (purgeUnavailableMaterials) {
            oldRightClicks.removeIf(m -> Material.getMaterial(m) == null);
        }
        List<String> newRightClicks = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation key = entry.getKey();
            Block block = entry.getValue();
            if (!key.getNamespace().equals("minecraft")) {
                String materialName = key.toString().toUpperCase().replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
                try {
                    Material material = Material.getMaterial(materialName);
                    Method m = block.getClass().getMethod(ObfHelper.getName("onBlockActivated", "func_180639_a"), World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class);
                    if (material != null && isMethodDeclaredInModBlock(m) && !addedMaterials.contains(materialName)) {
                        newRightClicks.add(materialName);
                        addedMaterials.add(materialName);
                    }
                } catch (Exception e)  {
                    HybridFix.LOGGER.warn("Failed to add custom right click for block {}", materialName, e);
                }
            }
        }
        newRightClicks.addAll(oldRightClicks);
        plugin.getConfig().set(customRightClickKey, newRightClicks);
        final long end = System.currentTimeMillis();
        HybridFix.LOGGER.info("Add {} custom right clicks for Residence, {} in total. (took {} ms)", newRightClicks.size() - oldRightClicks.size(), newRightClicks.size(), end - start);
    }

    public void addCustomBothClicks() {
        final long start = System.currentTimeMillis();
        List<String> oldBothClicks = plugin.getConfig().getStringList(customBothClickKey);
        if (purgeUnavailableMaterials) {
            oldBothClicks.removeIf(m -> Material.getMaterial(m) == null);
        }
        List<String> newBothClicks = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation key = entry.getKey();
            Block block = entry.getValue();
            if (!key.getNamespace().equals("minecraft")) {
                String materialName = key.toString().toUpperCase().replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
                try {
                    Material material = Material.getMaterial(materialName);
                    Method m = block.getClass().getMethod(ObfHelper.getName("onBlockActivated", "func_180639_a"), World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class);
                    Method m2 = block.getClass().getMethod(ObfHelper.getName("onBlockClicked", "func_180649_a"), World.class, BlockPos.class, EntityPlayer.class);
                    if (material != null && isMethodDeclaredInModBlock(m) && isMethodDeclaredInModBlock(m2) && !addedMaterials.contains(materialName)) {
                        newBothClicks.add(materialName);
                        addedMaterials.add(materialName);
                    }
                } catch (Exception e)  {
                    HybridFix.LOGGER.warn("Failed to add custom both click for block {}", materialName, e);
                }
            }
        }
        newBothClicks.addAll(oldBothClicks);
        plugin.getConfig().set(customBothClickKey, newBothClicks);
        final long end = System.currentTimeMillis();
        HybridFix.LOGGER.info("Add {} custom both clicks for Residence, {} in total. (took {} ms)", newBothClicks.size() - oldBothClicks.size(), newBothClicks.size(), end - start);
    }

    public void save() {
        plugin.saveConfig();
    }

    private boolean isMethodDeclaredInModBlock(Method method) {
        return method.getDeclaringClass() != Block.class;
    }
}
