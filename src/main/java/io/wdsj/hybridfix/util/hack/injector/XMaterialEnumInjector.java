package io.wdsj.hybridfix.util.hack.injector;
/*
import io.wdsj.hybridfix.HybridFix;
import org.bukkit.Material;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
*/

/**
 * Injects enum values into the XMaterial class.
 * <p>
 * Example usage:
 * <pre>
 *         il.add(new VarInsnNode(Opcodes.ALOAD, 0));
 *         il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/bukkit/Material", "name", "()Ljava/lang/String;", false));
 *
 *         // Type.getObjectType(classNode.name)
 *         il.add(new LdcInsnNode(Type.getObjectType(classNode.name)));
 *
 *         il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/wdsj/hybridfix/util/hack/injector/XMaterialEnumInjector", "getOrInject", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false));
 *
 *         il.add(new TypeInsnNode(Opcodes.CHECKCAST, classNode.name));
 *         il.add(new InsnNode(Opcodes.ARETURN));
 * <pre/>
 */
@SuppressWarnings({"unchecked", "unused"})
public class XMaterialEnumInjector {
/* // TODO: May be used later?
    private static final Unsafe unsafe;
    // java.lang.Enum field offset cache
    private static final long enumNameOffset;
    private static final long enumOrdinalOffset;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);

            Field nameField = Enum.class.getDeclaredField("name");
            enumNameOffset = unsafe.objectFieldOffset(nameField);

            Field ordinalField = Enum.class.getDeclaredField("ordinal");
            enumOrdinalOffset = unsafe.objectFieldOffset(ordinalField);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize XMaterialEnumInjector", e);
        }
    }

    public static Object getOrInject(String materialName, Class<?> enumClass) {
        try {
            Field namesField = enumClass.getDeclaredField("NAMES");
            namesField.setAccessible(true);
            Map<String, Object> namesMap = (Map<String, Object>) namesField.get(null);

            if (namesMap.containsKey(materialName)) {
                return namesMap.get(materialName);
            }

            Field valuesField = enumClass.getDeclaredField("$VALUES");
            Object base = unsafe.staticFieldBase(valuesField);
            long offset = unsafe.staticFieldOffset(valuesField);
            Object[] values = (Object[]) unsafe.getObject(base, offset);

            int ordinal = values.length;
            Object newEnum = unsafe.allocateInstance(enumClass);

            unsafe.putObject(newEnum, enumNameOffset, materialName);
            unsafe.putInt(newEnum, enumOrdinalOffset, ordinal);

            setFieldViaUnsafe(enumClass, newEnum, "data", (byte) 0);
            setFieldViaUnsafe(enumClass, newEnum, "legacy", new String[0]);

            Material bukkitMaterial = Material.getMaterial(materialName);
            setFieldViaUnsafe(enumClass, newEnum, "material", bukkitMaterial);

            Object[] newValues = Arrays.copyOf(values, values.length + 1);
            newValues[newValues.length - 1] = newEnum;

            unsafe.putObject(base, offset, newValues);

            cleanEnumCache(enumClass);

            namesMap.put(materialName, newEnum);

            updateGuavaCacheSafe(enumClass, materialName, newEnum);

            HybridFix.LOGGER.info("Injected XMaterial class {}: {}", enumClass.getName(), materialName);
            return newEnum;

        } catch (Throwable e) {
            HybridFix.LOGGER.error("Failed to inject XMaterial: {}", materialName, e);
            try {
                Field stoneField = enumClass.getDeclaredField("STONE");
                return unsafe.getObject(unsafe.staticFieldBase(stoneField), unsafe.staticFieldOffset(stoneField));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static void cleanEnumCache(Class<?> enumClass) {
        try {
            for (String fieldName : new String[]{"enumConstants", "enumConstantDirectory"}) {
                try {
                    Field field = Class.class.getDeclaredField(fieldName);
                    long offset = unsafe.objectFieldOffset(field);
                    unsafe.putObject(enumClass, offset, null);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static void updateGuavaCacheSafe(Class<?> enumClass, String key, Object value) {
        try {
            Field cacheField = enumClass.getDeclaredField("NAME_CACHE");
            long cacheOffset = unsafe.staticFieldOffset(cacheField);
            Object cacheBase = unsafe.staticFieldBase(cacheField);
            Object cache = unsafe.getObject(cacheBase, cacheOffset);

            if (cache != null) {
                Method putMethod = null;
                try {
                    putMethod = cache.getClass().getMethod("put", Object.class, Object.class);
                } catch (NoSuchMethodException e) {
                    HybridFix.LOGGER.warn("Unable to find Guava cache method, attempting to search for it...", e);
                    for (Method m : cache.getClass().getMethods()) {
                        if (m.getName().equals("put") && m.getParameterCount() == 2) {
                            putMethod = m;
                            HybridFix.LOGGER.info("Guava cache method found: {}", putMethod);
                            break;
                        }
                    }
                }

                if (putMethod != null) {
                    putMethod.setAccessible(true);
                    putMethod.invoke(cache, key, value);
                }
            }
        } catch (Throwable t) {
            HybridFix.LOGGER.warn("Failed to update Guava cache", t);
        }
    }

    private static void setFieldViaUnsafe(Class<?> clazz, Object instance, String fieldName, Object value) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        long offset = unsafe.objectFieldOffset(field);

        if (value instanceof Integer) {
            unsafe.putInt(instance, offset, (Integer) value);
        } else if (value instanceof Byte) {
            unsafe.putByte(instance, offset, (Byte) value);
        } else if (value instanceof Boolean) {
            unsafe.putBoolean(instance, offset, (Boolean) value);
        } else {
            unsafe.putObject(instance, offset, value);
        }
    }
 */
}