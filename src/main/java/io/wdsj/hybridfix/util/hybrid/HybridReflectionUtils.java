package io.wdsj.hybridfix.util.hybrid;

import io.wdsj.hybridfix.HybridFix;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class HybridReflectionUtils {
    private static final Field FIELD_WORLD_CAPTURE_TREE_GENERATION = getField(World.class, "captureTreeGeneration");
    private static final MethodHandle CONSTRUCTOR_CRAFT_BLOCK_STATE = getConstructorHandle(CraftBlockState.class, BlockSnapshot.class);

    public static void setCaptureTreeGeneration(World world, boolean value) {
        setBooleanField(FIELD_WORLD_CAPTURE_TREE_GENERATION, world, value);
    }
    public static CraftBlockState newBlockStateFromBlockSnapshot(BlockSnapshot snapshot) {
        assert CONSTRUCTOR_CRAFT_BLOCK_STATE != null;
        try {
            return (CraftBlockState) CONSTRUCTOR_CRAFT_BLOCK_STATE.invokeExact(snapshot);
        } catch (Throwable e) {
            HybridFix.LOGGER.warn("Error occurred while creating object from constructor {}", CONSTRUCTOR_CRAFT_BLOCK_STATE.toString());
            throw new RuntimeException(e);
        }
    }
    private static <T> MethodHandle getConstructorHandle(Class<T> clazz, Class<?>... params) {
        try {
            Constructor<T> c = clazz.getConstructor(params);
            c.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(c);
        } catch (Exception e) {
            return null;
        }
    }
    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            return null;
        }
    }
    private static void setField(Field field, Object instance, Object... value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting field {}", field.getName(), e);
        }
    }

    private static void setBooleanField(Field field, Object instance, boolean value) {
        try {
            field.setBoolean(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting field {}", field.getName(), e);
        }
    }
}
