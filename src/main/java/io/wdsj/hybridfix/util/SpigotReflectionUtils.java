package io.wdsj.hybridfix.util;

import io.wdsj.hybridfix.HybridFix;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import java.lang.reflect.Method;

public class SpigotReflectionUtils {
    private SpigotReflectionUtils() {}
    private static final Method METHOD_CRAFT_ITEM_STACK_AS_NMS_COPY = getMethod(CraftItemStack.class, "asNMSCopy", org.bukkit.inventory.ItemStack.class);

    public static ItemStack CraftItemStack_asNMSCopy(org.bukkit.inventory.ItemStack bukkitItemStack) {
        return (ItemStack) invokeMethod(METHOD_CRAFT_ITEM_STACK_AS_NMS_COPY, null, bukkitItemStack);
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


    private static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            HybridFix.LOGGER.error("Error occurred while invoking method {} through reflection, things may not work well.", method.getName());
            throw new RuntimeException(e);
        }
    }
}
