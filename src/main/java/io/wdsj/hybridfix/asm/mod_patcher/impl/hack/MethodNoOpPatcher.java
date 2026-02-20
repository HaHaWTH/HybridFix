package io.wdsj.hybridfix.asm.mod_patcher.impl.hack;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.mod_patcher.ConfigurableModPatcher;
import io.wdsj.hybridfix.asm.mod_patcher.annotation.ApplyToMod;
import io.wdsj.hybridfix.config.Settings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * No-op specified methods
 * Format: ClassName|MethodName(Desc)|Value
 */
@ApplyToMod.Configurable
public class MethodNoOpPatcher extends ConfigurableModPatcher {

    private final Map<String, List<TargetMethod>> targetMap;

    public MethodNoOpPatcher() {
        this.targetMap = new HashMap<>();
        String[] configs = Settings.asmModPatcherSettings.methodNoOpPatcherTargets;
        if (configs == null) return;

        for (String config : configs) {
            try {
                String[] parts = config.split("\\|");
                if (parts.length < 2) continue;

                String className = parts[0].trim();
                String methodFull = parts[1].trim();
                String value = parts.length > 2 ? parts[2].trim() : null;

                int descStart = methodFull.indexOf('(');
                String name = methodFull.substring(0, descStart);
                String desc = methodFull.substring(descStart);

                this.targetMap.computeIfAbsent(className, k -> new ArrayList<>())
                        .add(new TargetMethod(name, desc, value));
            } catch (Exception e) {
                HybridFix.LOGGER.error("Failed to parse MethodNoOpPatcher config: {}", config);
            }
        }
    }

    @Override
    public byte[] transform(String untransformedName, String className, byte[] basicClass) {
        List<TargetMethod> targets = targetMap.get(className);
        if (targets == null) return basicClass;

        ClassNode cn = new ClassNode();
        new ClassReader(basicClass).accept(cn, 0);
        boolean changed = false;

        for (MethodNode mn : cn.methods) {
            for (TargetMethod target : targets) {
                if (mn.name.equals(target.name) && mn.desc.equals(target.desc)) {
                    rewriteMethod(mn, target);
                    changed = true;
                }
            }
        }

        if (changed) {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            byte[] bytes = cw.toByteArray();
            log(className);
            dump(className, bytes);
            return bytes;
        }
        return basicClass;
    }

    private void rewriteMethod(MethodNode mn, TargetMethod target) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables.clear();

        Type returnType = Type.getReturnType(mn.desc);
        InsnList il = mn.instructions;

        switch (returnType.getSort()) {
            case Type.VOID:
                il.add(new InsnNode(Opcodes.RETURN));
                break;
            case Type.BOOLEAN:
                boolean boolVal = target.value != null && target.value.equalsIgnoreCase("true");
                il.add(new InsnNode(boolVal ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
                il.add(new InsnNode(Opcodes.IRETURN));
                break;
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                int intVal = target.value != null ? Integer.parseInt(target.value) : 0;
                pushInt(il, intVal);
                il.add(new InsnNode(Opcodes.IRETURN));
                break;
            case Type.LONG:
                long longVal = target.value != null ? Long.parseLong(target.value) : 0L;
                il.add(new LdcInsnNode(longVal));
                il.add(new InsnNode(Opcodes.LRETURN));
                break;
            case Type.FLOAT:
                float floatVal = target.value != null ? Float.parseFloat(target.value) : 0.0f;
                il.add(new LdcInsnNode(floatVal));
                il.add(new InsnNode(Opcodes.FRETURN));
                break;
            case Type.DOUBLE:
                double doubleVal = target.value != null ? Double.parseDouble(target.value) : 0.0;
                il.add(new LdcInsnNode(doubleVal));
                il.add(new InsnNode(Opcodes.DRETURN));
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                il.add(new InsnNode(Opcodes.ACONST_NULL));
                il.add(new InsnNode(Opcodes.ARETURN));
                break;
        }
    }

    private void pushInt(InsnList il, int value) {
        if (value >= -1 && value <= 5) {
            il.add(new InsnNode(Opcodes.ICONST_0 + value));
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            il.add(new IntInsnNode(Opcodes.BIPUSH, value));
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            il.add(new IntInsnNode(Opcodes.SIPUSH, value));
        } else {
            il.add(new LdcInsnNode(value));
        }
    }

    @Override
    public boolean isEnabled() {
        return Settings.asmModPatcherSettings.methodNoOpPatcher;
    }

    @Override
    public String[] getTargetClasses() {
        return targetMap.keySet().toArray(new String[0]);
    }

    private static class TargetMethod {
        final String name;
        final String desc;
        final String value;

        TargetMethod(String name, String desc, String value) {
            this.name = name;
            this.desc = desc;
            this.value = value;
        }
    }
}