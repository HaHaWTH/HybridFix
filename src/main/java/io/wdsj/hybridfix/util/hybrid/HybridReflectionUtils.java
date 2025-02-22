package io.wdsj.hybridfix.util.hybrid;

import com.google.common.base.Preconditions;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.HybridFixServer;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class HybridReflectionUtils {
    private static final Field FIELD_WORLD_CAPTURE_TREE_GENERATION = getField(World.class, "captureTreeGeneration");
    private static final Field FIELD_BLOCK_SAPLING_TREE_TYPE = getField(BlockSapling.class, "treeType");
    private static final MethodHandle CONSTRUCTOR_CRAFT_BLOCK_STATE = getConstructorHandle(CraftBlockState.class, BlockSnapshot.class);

    public static void setCaptureTreeGeneration(World world, boolean value) {
        Preconditions.checkNotNull(FIELD_WORLD_CAPTURE_TREE_GENERATION);
        setBooleanField(FIELD_WORLD_CAPTURE_TREE_GENERATION, world, value);
    }

    public static TreeType getTreeType() { // Currently unused
        Preconditions.checkNotNull(FIELD_BLOCK_SAPLING_TREE_TYPE);
        return (TreeType) getFieldValue(FIELD_BLOCK_SAPLING_TREE_TYPE, null);
    }

    public static void setTreeType(TreeType treeType) {
        Preconditions.checkNotNull(FIELD_BLOCK_SAPLING_TREE_TYPE);
        setField(FIELD_BLOCK_SAPLING_TREE_TYPE, null, treeType);
    }

    public static CraftBlockState newBlockStateFromBlockSnapshot(BlockSnapshot snapshot) {
        Preconditions.checkNotNull(CONSTRUCTOR_CRAFT_BLOCK_STATE);
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
            HybridFix.LOGGER.warn("Error occurred while getting constructor {}", clazz.getName());
            HybridFixServer.createServerDump(e);
            return null;
        }
    }
    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while getting field {}", fieldName);
            HybridFixServer.createServerDump(e);
            return null;
        }
    }

    private static Object getFieldValue(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while getting value of field {}", field.getName(), e);
            return null;
        }
    }

    private static void setField(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting value of field {}", field.getName(), e);
        }
    }

    private static void setBooleanField(Field field, Object instance, boolean value) {
        try {
            field.setBoolean(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting boolean value of field {}", field.getName(), e);
        }
    }
}
