package io.wdsj.hybridfix.asm.plugin_patcher.impl.residence.v6;

import io.wdsj.hybridfix.asm.plugin_patcher.AbstractPluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginClassWriter;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Remove 1.13+ BlockData instructions that causes NoClassDefFoundError.
 * @see <a href="https://github.com/Zrips/Residence/blob/master/src/main/java/com/bekvon/bukkit/residence/listeners/ResidenceBlockListener.java#L124">ResidenceBlockListener</a>
 */
@ApplyToPlugin("Residence")
public class ResidenceBlockDataPatcher extends AbstractPluginPatcher {

    private static final String TARGET_CLASS = "com.bekvon.bukkit.residence.listeners.ResidenceBlockListener";
    private static final String BLOCK_DATA_PKG = "org/bukkit/block/data";
    private static final String BLOCK_CLASS = "org/bukkit/block/Block";

    @Override
    public byte[] transform(String untransformedName, String className, byte[] basicClass) {
        if (TARGET_CLASS.equals(className)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if (patchBlockListener(classNode)) {
                ClassWriter classWriter = new PluginClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, getPluginClassLoader());
                classNode.accept(classWriter);
                byte[] bytes = classWriter.toByteArray();
                dump(className, bytes);
                log(className);
                return bytes;
            }
        }
        return basicClass;
    }

    @Override
    public boolean isEnabled() {
        return Settings.pluginPatcherSettings.patchResidenceV6;
    }

    private boolean patchBlockListener(ClassNode classNode) {
        boolean changed = false;

        for (MethodNode method : classNode.methods) {
            if (method.localVariables != null) {
                for (LocalVariableNode lvn : method.localVariables) {
                    if (lvn.desc != null && lvn.desc.contains(BLOCK_DATA_PKG)) {
                        lvn.desc = "Ljava/lang/Object;";
                        changed = true;
                    }
                }
            }

            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                String removeReason = null;

                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    if (min.owner.startsWith(BLOCK_DATA_PKG)) {
                        removeReason = "Method call: " + min.owner + "." + min.name;
                    } else if (min.owner.equals(BLOCK_CLASS) && (min.name.equals("getBlockData") || min.name.equals("setBlockData"))) {
                        removeReason = "Block API: " + min.name;
                    }
                } else if (insn instanceof TypeInsnNode) {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    if (tin.desc.startsWith(BLOCK_DATA_PKG)) {
                        removeReason = "Type usage: " + tin.desc;
                    }
                }

                if (removeReason != null) {
                    InsnList throwList = new InsnList();
                    throwList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/UnsupportedOperationException"));
                    throwList.add(new InsnNode(Opcodes.DUP));
                    throwList.add(new LdcInsnNode("Removed 1.13+ BlockData instruction: " + removeReason));
                    throwList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V", false));
                    throwList.add(new InsnNode(Opcodes.ATHROW));

                    method.instructions.insertBefore(insn, throwList);
                    iterator.remove();
                    changed = true;
                }
            }
        }
        return changed;
    }
}