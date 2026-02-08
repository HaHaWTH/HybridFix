package io.wdsj.hybridfix.util.reflection;

import io.wdsj.hybridfix.HybridFix;
import net.lenni0451.reflect.JavaBypass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A fluent API for reflection operations on a target class.
 * <p>
 * This class provides a chainable interface to access methods, fields, constructors,
 * and their corresponding {@link MethodHandle}s of a specified class. It supports
 * both {@link Class} and {@link String} representations for class and parameter types,
 * and allows toggling accessibility for private members. All reflection operations
 * no longer enforce exception handling on failure for simpler error handling.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * Constructor&lt;String&gt; constructor = ReflectionChain.fromClass(String.class)
 *     .params(byte[].class)
 *     .constructor(); // The type information will be kept
 * String str = constructor.newInstance(new byte[]{65, 66, 67});
 * </pre>
 * </p>
 *
 * @param <T> the type of the target class
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "unchecked"})
public class FluentReflect<T> {
    private FluentReflect() {
    }

    private static final Method PRIVATE_LOOKUP_IN;
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        Method privateLookupIn = null;
        MethodHandles.Lookup implLookup = null;

        try {
            // noinspection JavaReflectionMemberAccess
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            privateLookupIn.setAccessible(true);
        } catch (Throwable ignored) {
        }
        try {
            implLookup = JavaBypass.TRUSTED_LOOKUP;
            if (implLookup != null) HybridFix.LOGGER.debug("Found IMPL_LOOKUP");
        } catch (Throwable ignored) {
        }
        PRIVATE_LOOKUP_IN = privateLookupIn;
        IMPL_LOOKUP = implLookup;
    }

    /**
     * Creates a new reflection chain for the specified class.
     *
     * @param <T>   the type of the target class
     * @param clazz the target class, which must not be null
     * @return a new reflection chain for the specified class
     */
    public static <T> ReflectStream<T> fromClass(@NotNull Class<T> clazz) {
        return fromClass(clazz, null);
    }

    /**
     * Creates a new reflection chain for the specified class with a specific ClassLoader.
     *
     * @param <T>         the type of the target class
     * @param clazz       the target class, which must not be null
     * @param classLoader the class loader to use for resolving parameters, or null to use default
     * @return a new reflection chain for the specified class
     */
    public static <T> ReflectStream<T> fromClass(@NotNull Class<T> clazz, @Nullable ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "The class must not be null");
        return new ReflectStreamImpl<>(clazz, null, classLoader);
    }

    /**
     * Creates a new reflection chain for the class specified by its name.
     *
     * @param <T>       the type of the target class
     * @param className the full-qualified name of the target class, which must not be null
     * @return a new reflection chain for the specified class
     */
    public static <T> ReflectStream<?> fromClass(@NotNull String className) {
        return fromClass(className, null);
    }

    /**
     * Creates a new reflection chain for the class specified by its name with a specific ClassLoader.
     *
     * @param <T>         the type of the target class
     * @param className   the full-qualified name of the target class, which must not be null
     * @param classLoader the class loader to use for resolution, or null to use default
     * @return a new reflection chain for the specified class
     */
    public static <T> ReflectStream<?> fromClass(@NotNull String className, @Nullable ClassLoader classLoader) {
        Objects.requireNonNull(className, "The class name must not be null");
        return new ReflectStreamImpl<>(null, className, classLoader);
    }

    private static class ReflectStreamImpl<T> implements ReflectStream<T> {
        private final Class<T> targetClass;
        private final String targetClassName;
        private final ClassLoader classLoader;
        private String name;
        private boolean isAccessible = false;
        private boolean isTerminated = false;
        private Object returnType = void.class;

        ReflectStreamImpl(Class<T> clazz, String className, ClassLoader classLoader) {
            this.targetClass = clazz;
            this.targetClassName = className;
            this.classLoader = classLoader;
        }

        private void checkNotTerminated() {
            if (isTerminated) {
                throw new IllegalStateException("Stream has been terminated by a terminal operation and cannot be modified");
            }
        }

        private Class<?> resolveReturnType() {
            if (returnType instanceof Class<?>) {
                return (Class<?>) returnType;
            } else if (returnType instanceof String) {
                try {
                    return classLoader != null
                            ? Class.forName((String) returnType, false, classLoader)
                            : Class.forName((String) returnType);
                } catch (ClassNotFoundException e) {
                    SneakyThrow.throw0(e);
                    throw new RuntimeException(e); // unreachable
                }
            } else {
                throw new IllegalStateException("Invalid return type: " + returnType);
            }
        }

        @Override
        public ReflectStream<T> name(@NotNull String name) {
            checkNotTerminated();
            Objects.requireNonNull(name, "Name cannot be null");
            this.name = name;
            return this;
        }

        @Override
        public ReflectStream<T> accessible(boolean accessible) {
            checkNotTerminated();
            this.isAccessible = accessible;
            return this;
        }

        @Override
        public ReflectStream<T> returnType(@NotNull Class<?> returnType) {
            checkNotTerminated();
            this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
            return this;
        }

        @Override
        public ReflectStream<T> returnType(@NotNull String returnType) {
            checkNotTerminated();
            this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
            return this;
        }

        @Override
        public ParameterStream<T> param(@NotNull Object paramType) {
            checkNotTerminated();
            ParameterStreamImpl<T> chain = new ParameterStreamImpl<>(targetClass, targetClassName, name, isAccessible, classLoader);
            if (this.returnType instanceof Class<?>) {
                chain.returnType((Class<?>) this.returnType);
            } else {
                chain.returnType((String) this.returnType);
            }
            chain.param(paramType);
            markTerminated();
            return chain;
        }

        @Override
        public ParameterStream<T> params(Object... paramTypes) {
            checkNotTerminated();
            ParameterStreamImpl<T> chain = new ParameterStreamImpl<>(targetClass, targetClassName, name, isAccessible, classLoader);
            if (this.returnType instanceof Class<?>) {
                chain.returnType((Class<?>) this.returnType);
            } else {
                chain.returnType((String) this.returnType);
            }
            chain.params(paramTypes);
            markTerminated();
            return chain;
        }

        @Override
        public ParameterStream<T> params(Class<?>... paramTypes) {
            checkNotTerminated();
            ParameterStreamImpl<T> chain = new ParameterStreamImpl<>(targetClass, targetClassName, name, isAccessible, classLoader);
            if (this.returnType instanceof Class<?>) {
                chain.returnType((Class<?>) this.returnType);
            } else {
                chain.returnType((String) this.returnType);
            }
            chain.params(paramTypes);
            markTerminated();
            return chain;
        }

        @Override
        public ParameterStream<T> params(String... paramTypes) {
            checkNotTerminated();
            ParameterStreamImpl<T> chain = new ParameterStreamImpl<>(targetClass, targetClassName, name, isAccessible, classLoader);
            if (this.returnType instanceof Class<?>) {
                chain.returnType((Class<?>) this.returnType);
            } else {
                chain.returnType((String) this.returnType);
            }
            chain.params(paramTypes);
            markTerminated();
            return chain;
        }

        private Class<T> resolveTargetClass() {
            if (targetClass != null) {
                return targetClass;
            }
            if (targetClassName == null) {
                throw new IllegalStateException("Class or class name must be specified");
            }
            try {
                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) (classLoader != null
                        ? Class.forName(targetClassName, false, classLoader)
                        : Class.forName(targetClassName));
                return clazz;
            } catch (ClassNotFoundException e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        private void markTerminated() {
            isTerminated = true;
        }

        @Override
        public Method method() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                return resolveTargetClass().getMethod(name);
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Method declaredMethod() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Method method = resolveTargetClass().getDeclaredMethod(name);
                if (isAccessible) method.setAccessible(true);
                return method;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Field field() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Field field = resolveTargetClass().getField(name);
                if (isAccessible) field.setAccessible(true);
                return field;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Field declaredField() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Field field = resolveTargetClass().getDeclaredField(name);
                if (isAccessible) field.setAccessible(true);
                return field;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Constructor<T> constructor() {
            checkNotTerminated();
            markTerminated();
            try {
                Constructor<T> constructor = resolveTargetClass().getDeclaredConstructor();
                if (isAccessible) constructor.setAccessible(true);
                return constructor;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle virtualMethodHandle() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findVirtual(targetClazz, name, MethodType.methodType(resolveReturnType()));
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle staticMethodHandle() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findStatic(targetClazz, name, MethodType.methodType(resolveReturnType()));
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle virtualFieldGetter() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findGetter(targetClazz, name, resolveReturnType());
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle staticFieldGetter() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findStaticGetter(targetClazz, name, resolveReturnType());
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle virtualFieldSetter() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findSetter(targetClazz, name, resolveReturnType());
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle staticFieldSetter() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findStaticSetter(targetClazz, name, resolveReturnType());
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle constructorHandle() {
            checkNotTerminated();
            markTerminated();
            try {
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findConstructor(targetClazz, MethodType.methodType(void.class));
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public UnsafeFieldAccessor virtualFieldAccessor() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Field field = resolveTargetClass().getDeclaredField(name);
                return UnsafeFieldAccessorFactory.create(field, false);
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public UnsafeFieldAccessor staticFieldAccessor() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Field field = resolveTargetClass().getDeclaredField(name);
                return UnsafeFieldAccessorFactory.create(field, true);
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        private MethodHandles.Lookup getLookup(Class<T> clazz) {
            if (IMPL_LOOKUP != null) {
                return IMPL_LOOKUP;
            }

            try {
                if (PRIVATE_LOOKUP_IN != null) {
                    return (MethodHandles.Lookup) PRIVATE_LOOKUP_IN.invoke(null, clazz, MethodHandles.lookup());
                }
            } catch (Exception ignored) {
            }
            return MethodHandles.lookup();
        }
    }

    private static class ParameterStreamImpl<T> implements ParameterStream<T> {
        private final Class<T> targetClass;
        private final String targetClassName;
        private final ClassLoader classLoader;
        private String name;
        private final List<Object> parameterTypes = new ArrayList<>();
        private boolean isAccessible;
        private boolean isTerminated = false;
        private Object returnType = void.class;

        ParameterStreamImpl(Class<T> targetClass, String targetClassName, String name, boolean isAccessible, ClassLoader classLoader) {
            this.targetClass = targetClass;
            this.targetClassName = targetClassName;
            this.name = name;
            this.isAccessible = isAccessible;
            this.classLoader = classLoader;
        }

        private void checkNotTerminated() {
            if (this.isTerminated) {
                throw new IllegalStateException("Stream has been terminated by a terminal operation and cannot be modified");
            }
        }

        private Class<?> resolveReturnType() {
            if (returnType instanceof Class<?>) {
                return (Class<?>) returnType;
            } else if (returnType instanceof String) {
                try {
                    return classLoader != null
                            ? Class.forName((String) returnType, false, classLoader)
                            : Class.forName((String) returnType);
                } catch (ClassNotFoundException e) {
                    SneakyThrow.throw0(e);
                    throw new RuntimeException(e); // unreachable
                }
            } else {
                throw new IllegalStateException("Invalid return type: " + returnType);
            }
        }

        @Override
        public ParameterStream<T> name(@NotNull String name) {
            checkNotTerminated();
            Objects.requireNonNull(name, "Name cannot be null");
            this.name = name;
            return this;
        }

        @Override
        public ParameterStream<T> accessible(boolean accessible) {
            checkNotTerminated();
            this.isAccessible = accessible;
            return this;
        }

        @Override
        public ParameterStream<T> returnType(@NotNull Class<?> returnType) {
            checkNotTerminated();
            this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
            return this;
        }

        @Override
        public ParameterStream<T> returnType(@NotNull String returnType) {
            checkNotTerminated();
            this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
            return this;
        }

        @Override
        public ParameterStream<T> param(@NotNull Object paramType) {
            checkNotTerminated();
            Objects.requireNonNull(paramType, "Parameter type cannot be null");
            if (paramType instanceof Class<?> || paramType instanceof String) {
                this.parameterTypes.add(paramType);
            } else {
                throw new IllegalArgumentException("Parameter must be Class or String");
            }
            return this;
        }

        @Override
        public ParameterStream<T> params(Object... paramTypes) {
            checkNotTerminated();
            for (Object paramType : paramTypes) {
                param(paramType);
            }
            return this;
        }

        @Override
        public ParameterStream<T> params(Class<?>... paramTypes) {
            checkNotTerminated();
            this.parameterTypes.addAll(Arrays.asList(paramTypes));
            return this;
        }

        @Override
        public ParameterStream<T> params(String... paramTypes) {
            checkNotTerminated();
            this.parameterTypes.addAll(Arrays.asList(paramTypes));
            return this;
        }

        private Class<T> resolveTargetClass() {
            if (targetClass != null) {
                return targetClass;
            }
            if (targetClassName == null) {
                throw new IllegalStateException("Class or class name must be specified");
            }
            try {
                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) (classLoader != null
                        ? Class.forName(targetClassName, false, classLoader)
                        : Class.forName(targetClassName));
                return clazz;
            } catch (ClassNotFoundException e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        private Class<?>[] resolveParameterTypes() {
            List<Class<?>> resolvedTypes = new ArrayList<>();
            for (Object paramType : parameterTypes) {
                if (paramType instanceof Class<?>) {
                    resolvedTypes.add((Class<?>) paramType);
                } else if (paramType instanceof String) {
                    try {
                        resolvedTypes.add(classLoader != null
                                ? Class.forName((String) paramType, false, classLoader)
                                : Class.forName((String) paramType));
                    } catch (ClassNotFoundException e) {
                        SneakyThrow.throw0(e);
                        throw new RuntimeException(e); // unreachable
                    }
                } else {
                    throw new IllegalStateException("Invalid parameter type: " + paramType);
                }
            }
            return resolvedTypes.toArray(new Class[0]);
        }

        private void markTerminated() {
            this.isTerminated = true;
        }

        @Override
        public Method method() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                return resolveTargetClass().getMethod(name, resolveParameterTypes());
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Method declaredMethod() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Method method = resolveTargetClass().getDeclaredMethod(name, resolveParameterTypes());
                if (isAccessible) method.setAccessible(true);
                return method;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Constructor<T> constructor() {
            checkNotTerminated();
            markTerminated();
            try {
                Constructor<T> constructor = resolveTargetClass().getDeclaredConstructor(resolveParameterTypes());
                if (isAccessible) constructor.setAccessible(true);
                return constructor;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle virtualMethodHandle() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findVirtual(targetClazz, name, MethodType.methodType(resolveReturnType(), resolveParameterTypes()));
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle staticMethodHandle() {
            checkNotTerminated();
            markTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findStatic(targetClazz, name, MethodType.methodType(resolveReturnType(), resolveParameterTypes()));
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle constructorHandle() {
            checkNotTerminated();
            markTerminated();
            try {
                Class<T> targetClazz = resolveTargetClass();
                return getLookup(targetClazz).findConstructor(targetClazz, MethodType.methodType(void.class, resolveParameterTypes()));
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        private MethodHandles.Lookup getLookup(Class<T> clazz) {
            if (IMPL_LOOKUP != null) {
                return IMPL_LOOKUP;
            }

            try {
                if (PRIVATE_LOOKUP_IN != null) {
                    return (MethodHandles.Lookup) PRIVATE_LOOKUP_IN.invoke(null, clazz, MethodHandles.lookup());
                }
            } catch (Exception ignored) {
            }
            return MethodHandles.lookup();
        }
    }

    private static class SneakyThrow {
        private SneakyThrow() {
        }

        @SuppressWarnings("unchecked")
        public static <T extends Throwable> void throw0(Throwable t) throws T {
            throw (T) t;
        }
    }
}