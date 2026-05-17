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
                String[] parts = config.split("\\|", 3);
                if (parts.length < 2) {
                    HybridFix.LOGGER.warn("Skipping incomplete configuration: {}", config);
                    continue;
                }

                String className = parts[0].trim();
                String methodPart = parts[1].trim();
                String value = parts.length > 2 ? parts[2].trim() : null;

                String name;
                String desc = null;

                if (methodPart.contains("(")) {
                    int descStart = methodPart.indexOf('(');
                    name = methodPart.substring(0, descStart);
                    desc = methodPart.substring(descStart);
                } else {
                    name = methodPart;
                }

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
                if (mn.name.equals(target.name) && (target.desc == null || mn.desc.equals(target.desc))) {
                    if ((mn.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
                        HybridFix.LOGGER.warn("Skipping abstract/native method: {}.{}{}", className, mn.name, mn.desc);
                        continue;
                    }
                    rewriteMethod(mn, target);
                    changed = true;
                    HybridFix.LOGGER.info("Applied No-Op to method: {}.{}{}", className, mn.name, mn.desc);
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
                boolean boolVal = target.value != null && (target.value.equalsIgnoreCase("true") || target.value.equals("1"));
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
            case Type.OBJECT:
                if (returnType.getInternalName().equals("java/lang/String") && target.value != null) {
                    il.add(new LdcInsnNode(target.value));
                } else {
                    il.add(new InsnNode(Opcodes.ACONST_NULL));
                }
                il.add(new InsnNode(Opcodes.ARETURN));
                break;
            case Type.ARRAY:
                if (target.value != null && (target.value.equalsIgnoreCase("empty") || target.value.equals("[]"))) {
                    il.add(new InsnNode(Opcodes.ICONST_0));
                    Type componentType = Type.getType(returnType.getDescriptor().substring(1));
                    if (componentType.getSort() == Type.OBJECT || componentType.getSort() == Type.ARRAY) {
                        il.add(new TypeInsnNode(Opcodes.ANEWARRAY, componentType.getInternalName()));
                    } else {
                        int typeCode;
                        switch (componentType.getSort()) {
                            case Type.BOOLEAN:
                                typeCode = Opcodes.T_BOOLEAN;
                                break;
                            case Type.CHAR:
                                typeCode = Opcodes.T_CHAR;
                                break;
                            case Type.BYTE:
                                typeCode = Opcodes.T_BYTE;
                                break;
                            case Type.SHORT:
                                typeCode = Opcodes.T_SHORT;
                                break;
                            case Type.INT:
                                typeCode = Opcodes.T_INT;
                                break;
                            case Type.FLOAT:
                                typeCode = Opcodes.T_FLOAT;
                                break;
                            case Type.LONG:
                                typeCode = Opcodes.T_LONG;
                                break;
                            case Type.DOUBLE:
                                typeCode = Opcodes.T_DOUBLE;
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown primitive array type: " + componentType);
                        }
                        il.add(new IntInsnNode(Opcodes.NEWARRAY, typeCode));
                    }
                } else {
                    il.add(new InsnNode(Opcodes.ACONST_NULL));
                }
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