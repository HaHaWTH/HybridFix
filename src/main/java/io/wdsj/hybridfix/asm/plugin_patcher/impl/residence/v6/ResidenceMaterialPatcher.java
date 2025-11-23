package io.wdsj.hybridfix.asm.plugin_patcher.impl.residence.v6;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.IBytecodePatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

@ApplyToPlugin("Residence")
@SuppressWarnings("unused")
public class ResidenceMaterialPatcher implements IBytecodePatcher {
    private static final String TARGET_CLASS_PACKAGE = "com.bekvon.bukkit.residence";

    @Override
    public byte[] transform(String className, byte[] basicClass) {
        if (className.startsWith(TARGET_CLASS_PACKAGE)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if (remapMaterials(classNode)) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);
                byte[] bytes = classWriter.toByteArray();
                dump(className, bytes);
                HybridFix.LOGGER.info("Transformed class {}", className);
                return bytes;
            }
        }
        return basicClass;
    }

    @Override
    public boolean isEnabled() {
        return Settings.pluginPatcherSettings.patchResidenceV6;
    }

    private boolean remapMaterials(ClassNode classNode) {
        boolean changed = false;
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();

                if (insn.getOpcode() == Opcodes.GETSTATIC) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;

                    if ("org/bukkit/Material".equals(fieldInsn.owner)) {
                        if ("SPAWNER".equals(fieldInsn.name)) {
                            fieldInsn.name = "MOB_SPAWNER";
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }
}