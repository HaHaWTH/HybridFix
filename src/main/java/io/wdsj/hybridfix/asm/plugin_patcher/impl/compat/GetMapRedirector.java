package io.wdsj.hybridfix.asm.plugin_patcher.impl.compat;

import io.wdsj.hybridfix.asm.plugin_patcher.ConfigurablePluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginClassWriter;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

@ApplyToPlugin.Configurable
public class GetMapRedirector extends ConfigurablePluginPatcher {
    @Override
    public String[] getTargetPlugins() {
        return Settings.pluginPatcherSettings.mapPalettePatchPlugins;
    }

    @Override
    public byte[] transform(String untransformedName, String className, byte[] basicClass) {
        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        boolean modified = false;

        for (MethodNode mn : cn.methods) {
            ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    if (min.getOpcode() == Opcodes.INVOKESTATIC &&
                            min.owner.equals("org/bukkit/Bukkit") &&
                            min.name.equals("getMap") &&
                            min.desc.equals("(I)Lorg/bukkit/map/MapView;")) {
                        min.desc = "(S)Lorg/bukkit/map/MapView;";
                        mn.instructions.insertBefore(min, new InsnNode(Opcodes.I2S));
                        modified = true;
                    }
                }
            }
        }

        if (modified) {
            ClassWriter classWriter = new PluginClassWriter(0, getPluginClassLoader());
            cn.accept(classWriter);
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
}
