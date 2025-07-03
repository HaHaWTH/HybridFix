package io.wdsj.hybridfix.util.reflection;

import com.google.common.base.Preconditions;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.SneakyThrow;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.ApiStatus;

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
    private static final MethodHandle MD_FIRE_EVENT = ReflectionChain.fromClass(SimplePluginManager.class)
            .name("fireEvent")
            .param(Event.class)
            .accessible(true)
            .methodHandle();

    public static void setCaptureTreeGeneration(World world, boolean value) {
        Preconditions.checkNotNull(FIELD_WORLD_CAPTURE_TREE_GENERATION);
        setBooleanFieldValue(FIELD_WORLD_CAPTURE_TREE_GENERATION, world, value);
    }

    public static void setTreeType(TreeType treeType) {
        Preconditions.checkNotNull(FIELD_BLOCK_SAPLING_TREE_TYPE);
        setFieldValue(FIELD_BLOCK_SAPLING_TREE_TYPE, null, treeType);
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

    /**
     * Direct call event, without dumb logic added by server software
     * @param event event to call
     */
    @ApiStatus.Internal
    public static void callEventDirect(Event event) {
        if (event.getHandlers().getRegisteredListeners().length == 0) return;
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimplePluginManager simplePluginManager = (SimplePluginManager) pluginManager;
        try {
            if (event.isAsynchronous() || !Bukkit.isPrimaryThread()) {
                if (Thread.holdsLock(pluginManager)) {
                    throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from inside synchronized code.");
                }
                if (Bukkit.isPrimaryThread()) {
                    throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from primary server thread.");
                }
                MD_FIRE_EVENT.invokeExact(simplePluginManager, event);
            } else {
                synchronized (pluginManager) {
                    MD_FIRE_EVENT.invokeExact(simplePluginManager, event);
                }
            }
        } catch (Throwable th) {
            SneakyThrow.sneaky(th);
        }
    }
}
