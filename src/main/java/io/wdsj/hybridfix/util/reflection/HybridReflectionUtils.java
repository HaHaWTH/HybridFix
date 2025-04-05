package io.wdsj.hybridfix.util.reflection;

import com.google.common.base.Preconditions;
import io.wdsj.hybridfix.HybridFix;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static io.wdsj.hybridfix.util.reflection.BaseReflectionUtils.*;

public class HybridReflectionUtils {
    private static final Field FIELD_WORLD_CAPTURE_TREE_GENERATION = ReflectionChain.fromClass(World.class)
            .name("captureTreeGeneration")
            .accessible(true)
            .field();
    private static final Field FIELD_BLOCK_SAPLING_TREE_TYPE = ReflectionChain.fromClass(BlockSapling.class)
            .name("treeType")
            .accessible(true)
            .field();
    private static final MethodHandle CTOR_CRAFT_BLOCK_STATE = ReflectionChain.fromClass(CraftBlockState.class)
            .param(BlockSnapshot.class)
            .accessible(true)
            .constructorHandle();

    public static void setCaptureTreeGeneration(World world, boolean value) {
        Preconditions.checkNotNull(FIELD_WORLD_CAPTURE_TREE_GENERATION);
        setBooleanField(FIELD_WORLD_CAPTURE_TREE_GENERATION, world, value);
    }

    @SuppressWarnings("unused")
    public static TreeType getTreeType() {
        Preconditions.checkNotNull(FIELD_BLOCK_SAPLING_TREE_TYPE);
        return (TreeType) getFieldValue(FIELD_BLOCK_SAPLING_TREE_TYPE, null);
    }

    public static void setTreeType(TreeType treeType) {
        Preconditions.checkNotNull(FIELD_BLOCK_SAPLING_TREE_TYPE);
        setField(FIELD_BLOCK_SAPLING_TREE_TYPE, null, treeType);
    }

    public static CraftBlockState newBlockStateFromBlockSnapshot(BlockSnapshot snapshot) {
        Preconditions.checkNotNull(CTOR_CRAFT_BLOCK_STATE);
        try {
            return (CraftBlockState) CTOR_CRAFT_BLOCK_STATE.invokeExact(snapshot);
        } catch (Throwable e) {
            HybridFix.LOGGER.warn("Error occurred while creating object from constructor {}", CTOR_CRAFT_BLOCK_STATE.toString());
            throw new RuntimeException(e);
        }
    }
}
