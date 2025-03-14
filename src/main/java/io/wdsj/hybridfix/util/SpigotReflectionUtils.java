package io.wdsj.hybridfix.util;

import io.wdsj.hybridfix.HybridFix;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Bridge for converting CraftBukkit method that returns {@link net.minecraft.server.v1_12_R1} objects to Forge {@link net.minecraft} objects without package relocation.
 * As we don't want to include hybrid server as a dependency.
 */
public class SpigotReflectionUtils {
    private SpigotReflectionUtils() {}
    private static final Method METHOD_CRAFT_ITEM_STACK_AS_NMS_COPY = getMethod(CraftItemStack.class, "asNMSCopy", org.bukkit.inventory.ItemStack.class);
    private static final Method METHOD_CRAFT_MAGIC_NUMBERS_GET_MATERIAL = getMethod(CraftMagicNumbers.class, "getMaterial", Block.class);
    private static final MethodHandle MH_CRAFT_PLAYER_GET_HANDLE = getMethodHandle(CraftPlayer.class, "getHandle");
    public static ItemStack CraftItemStack_asNMSCopy(org.bukkit.inventory.ItemStack bukkitItemStack) {
        return (ItemStack) invokeStaticMethod(METHOD_CRAFT_ITEM_STACK_AS_NMS_COPY, bukkitItemStack);
    }

    public static Material CraftMagicNumbers_getMaterial(Block block) {
        return (Material) invokeStaticMethod(METHOD_CRAFT_MAGIC_NUMBERS_GET_MATERIAL, block);
    }

    public static EntityPlayerMP CraftPlayer_getHandle(CraftPlayer craftPlayer) {
        try {
            return (EntityPlayerMP) MH_CRAFT_PLAYER_GET_HANDLE.invokeExact(craftPlayer);
        } catch (Throwable e) {
            HybridFix.LOGGER.error("Error occurred while invoking method CraftPlayer.getHandle through reflection, things may not work well.");
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            HybridFix.LOGGER.error("Error occurred while retrieving method {} through reflection, things may not work well.", methodName);
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle getMethodHandle(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return MethodHandles.lookup().unreflect(m);
        } catch (Exception e) {
            HybridFix.LOGGER.error("Error occurred while retrieving method handle {} through reflection, things may not work well.", methodName);
            throw new RuntimeException(e);
        }
    }

    private static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            HybridFix.LOGGER.error("Error occurred while invoking method {} through reflection, things may not work well.", method.getName());
            throw new RuntimeException(e);
        }
    }

    private static Object invokeStaticMethod(Method method, Object... args) {
        return invokeMethod(method, null, args);
    }
}
