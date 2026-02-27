package io.wdsj.hybridfix.asm.plugin_patcher.impl.hack;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.IBytecodePatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.ConfigurablePluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginClassWriter;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

@ApplyToPlugin.Configurable
public class ReflectFieldPatcher extends ConfigurablePluginPatcher {

    private static final String OUR_REFLECTION_CLASS = "com/cleanroommc/hackery/ReflectionHackery";
    private static final Map<String, String> PRIMITIVE_MAP = new HashMap<>();

    static {
        PRIMITIVE_MAP.put("Boolean", "Z");
        PRIMITIVE_MAP.put("Byte", "B");
        PRIMITIVE_MAP.put("Char", "C");
        PRIMITIVE_MAP.put("Short", "S");
        PRIMITIVE_MAP.put("Int", "I");
        PRIMITIVE_MAP.put("Long", "J");
        PRIMITIVE_MAP.put("Float", "F");
        PRIMITIVE_MAP.put("Double", "D");
    }

    @Override
    public byte[] transform(String untransformedName, String className, byte[] basicClass) {
        if (!IBytecodePatcher.isCommonPackage(className)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if (patchReflectCalls(classNode)) {
                ClassWriter classWriter = new PluginClassWriter(ClassWriter.COMPUTE_MAXS, getPluginClassLoader());
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
        return HybridFix.IS_CLEANROOM && Settings.pluginPatcherSettings.patchReflectField;
    }

    @Override
    public String[] getTargetPlugins() {
        return Settings.pluginPatcherSettings.patchReflectFieldPlugins;
    }

    private boolean patchReflectCalls(ClassNode classNode) {
        boolean changed = false;

        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();

                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;

                    if ("java/lang/reflect/Field".equals(methodInsn.owner)) {
                        String name = methodInsn.name;

                        boolean isGetOrSet = name.startsWith("set") || name.startsWith("get");
                        if (isGetOrSet && (name.length() == 3 || PRIMITIVE_MAP.containsKey(name.substring(3)))) {

                            String newName = name + "Field";

                            String typeDesc;
                            if (name.length() == 3) {
                                typeDesc = "Ljava/lang/Object;";
                            } else {
                                typeDesc = PRIMITIVE_MAP.get(name.substring(3));
                            }

                            String newDesc;
                            if (name.startsWith("s")) {
                                // Setter: (Field, ObjectTarget, Value)V
                                newDesc = "(Ljava/lang/reflect/Field;Ljava/lang/Object;" + typeDesc + ")V";
                            } else {
                                // Getter: (Field, ObjectTarget)Value
                                newDesc = "(Ljava/lang/reflect/Field;Ljava/lang/Object;)" + typeDesc;
                            }

                            methodInsn.setOpcode(Opcodes.INVOKESTATIC);
                            methodInsn.owner = OUR_REFLECTION_CLASS;
                            methodInsn.name = newName;
                            methodInsn.desc = newDesc;
                            methodInsn.itf = false;

                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }
}