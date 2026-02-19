package io.wdsj.hybridfix.asm.plugin_patcher.impl.compat;

import io.wdsj.hybridfix.asm.plugin_patcher.ConfigurablePluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginClassWriter;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.bukkit.map.MapPalette;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.awt.*;
import java.util.ListIterator;

@ApplyToPlugin.Configurable
public class MapPaletteRedirector extends ConfigurablePluginPatcher {

    private static final String TARGET_OWNER = "org/bukkit/map/MapPalette";
    private static final String TARGET_NAME = "getNearestColor";
    private static final String TARGET_DESC = "(Ljava/awt/Color;)Ljava/awt/Color;";

    private static final String HOOK_OWNER = "io/wdsj/hybridfix/asm/plugin_patcher/impl/compat/MapPaletteRedirector$Hooks";

    @Override
    public String[] getTargetPlugins() {
        return Settings.pluginPatcherSettings.mapPalettePatchPlugins;
    }

    @Override
    public byte[] transform(String untransformedName, String className, byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);

        boolean changed = false;
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();

                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) insn;

                    if (min.getOpcode() == Opcodes.INVOKESTATIC &&
                            TARGET_OWNER.equals(min.owner) &&
                            TARGET_NAME.equals(min.name) &&
                            TARGET_DESC.equals(min.desc)) {
                        min.owner = HOOK_OWNER;
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            ClassWriter classWriter = new PluginClassWriter(ClassWriter.COMPUTE_MAXS, getPluginClassLoader());
            classNode.accept(classWriter);
            byte[] bytes = classWriter.toByteArray();
            dump(className, bytes);
            log(className);
            return bytes;
        }

        return basicClass;
    }

    @Override
    public boolean isEnabled() {
        return Settings.pluginPatcherSettings.enableMapPalettePatch;
    }

    @SuppressWarnings({"deprecation", "unused"})
    public static class Hooks {
        @NotNull
        public static Color getNearestColor(@NotNull Color color) {
            byte b = MapPalette.matchColor(color);
            return MapPalette.getColor(b);
        }
    }
}