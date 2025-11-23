package io.wdsj.hybridfix.asm.plugin_patcher.impl.residence.v6;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.plugin_patcher.IPluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Remove 1.13+ BlockData instructions that causes NoClassDefFoundError.
 */
@ApplyToPlugin("Residence")
public class ResidenceBlockDataPatcher implements IPluginPatcher {

    private static final String TARGET_CLASS = "com.bekvon.bukkit.residence.listeners.ResidenceBlockListener";
    private static final String BLOCK_DATA_PKG = "org/bukkit/block/data";
    private static final String BLOCK_CLASS = "org/bukkit/block/Block";
    private ClassLoader classLoader;

    @Override
    public byte[] transform(String className, byte[] basicClass) {
        if (TARGET_CLASS.equals(className)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if (patchBlockListener(classNode)) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
                    @Override
                    protected String getCommonSuperClass(String type1, String type2) {
                        ClassLoader classLoader = getPluginClassLoader();

                        Class<?> c;
                        Class<?> d;
                        try {
                            c = Class.forName(type1.replace('/', '.'), false, classLoader);
                            d = Class.forName(type2.replace('/', '.'), false, classLoader);
                        } catch (Exception e) {
                            throw new RuntimeException(e.toString());
                        }

                        if (c.isAssignableFrom(d)) {
                            return type1;
                        } else if (d.isAssignableFrom(c)) {
                            return type2;
                        } else if (!c.isInterface() && !d.isInterface()) {
                            do {
                                c = c.getSuperclass();
                            } while(!c.isAssignableFrom(d));

                            return c.getName().replace('.', '/');
                        } else {
                            return "java/lang/Object";
                        }
                    }
                };
                classNode.accept(classWriter);
                byte[] bytes = classWriter.toByteArray();
                dump(className, bytes);
                this.classLoader = null;
                HybridFix.LOGGER.info("Transformed class {}", className);
                return bytes;
            }
        }
        this.classLoader = null;
        return basicClass;
    }

    @Override
    public boolean isEnabled() {
        return Settings.pluginPatcherSettings.patchResidenceV6;
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return this.classLoader;
    }

    @Override
    public void setPluginClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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