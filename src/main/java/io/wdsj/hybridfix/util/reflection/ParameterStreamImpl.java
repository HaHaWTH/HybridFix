package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue", "unchecked"})
class ParameterStreamImpl<T> implements ParameterStream<T> {
    private final Class<T> targetClass;
    private final String targetClassName;
    private final ClassLoader classLoader;
    private String name;
    private final List<Object> parameterTypes = new ArrayList<>();
    private boolean isAccessible;
    private boolean isTerminated = false;
    private Object returnType = void.class;
    private boolean initialize = false;

    private Class<?> typeContract;

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
            } catch (Throwable t) {
                SneakyThrow.throw0(t);
                throw new RuntimeException(t); // unreachable
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

    @Override
    public <U> ParameterStream<U> as(@NotNull Class<U> type) {
        checkNotTerminated();
        this.typeContract = type;
        return (ParameterStream<U>) this;
    }

    @Override
    public ParameterStream<T> initialize(boolean initialize) {
        checkNotTerminated();
        this.initialize = initialize;
        return this;
    }

    private Class<T> resolveTargetClass() {
        if (targetClass != null) {
            checkTypeContract(targetClass);
            return targetClass;
        }
        if (targetClassName == null) {
            throw new IllegalStateException("Class or class name must be specified");
        }
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) (classLoader != null
                    ? Class.forName(targetClassName, initialize, classLoader)
                    : Class.forName(targetClassName, initialize, getClass().getClassLoader()));
            checkTypeContract(clazz);
            return clazz;
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
        }
    }

    private void checkTypeContract(Class<T> clazz) {
        if (typeContract != null && !typeContract.isAssignableFrom(clazz)) {
            throw new ClassCastException("Class " + clazz.getName() +
                    " does not match type contract: " + typeContract.getName());
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
                            ? Class.forName((String) paramType, initialize, classLoader)
                            : Class.forName((String) paramType, initialize, getClass().getClassLoader()));
                } catch (Throwable t) {
                    SneakyThrow.throw0(t);
                    throw new RuntimeException(t); // unreachable
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
            Method method = resolveTargetClass().getMethod(name, resolveParameterTypes());
            if (isAccessible) method.setAccessible(true);
            return method;
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
        }
    }

    @Override
    public MethodHandle constructorHandle() {
        checkNotTerminated();
        markTerminated();
        try {
            Class<T> targetClazz = resolveTargetClass();
            return getLookup(targetClazz).findConstructor(targetClazz, MethodType.methodType(void.class, resolveParameterTypes()));
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
        }
    }

    private <R> ReflectHolder<T, R> findTerminal(Supplier<R> supplier) {
        try {
            return ReflectHolder.success(supplier.get());
        } catch (Throwable t) {
            return ReflectHolder.failure(t);
        }
    }

    @Override
    public ReflectHolder<T, Method> findMethod() {
        return findTerminal(this::method);
    }

    @Override
    public ReflectHolder<T, Method> findDeclaredMethod() {
        return findTerminal(this::declaredMethod);
    }

    @Override
    public ReflectHolder<T, Constructor<T>> findConstructor() {
        return findTerminal(this::constructor);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findVirtualMethodHandle() {
        return findTerminal(this::virtualMethodHandle);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findStaticMethodHandle() {
        return findTerminal(this::staticMethodHandle);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findConstructorHandle() {
        return findTerminal(this::constructorHandle);
    }

    private MethodHandles.Lookup getLookup(Class<T> clazz) {
        if (FluentReflect.IMPL_LOOKUP != null) {
            return FluentReflect.IMPL_LOOKUP;
        }

        try {
            if (FluentReflect.PRIVATE_LOOKUP_IN != null) {
                return (MethodHandles.Lookup) FluentReflect.PRIVATE_LOOKUP_IN.invoke(null, clazz, MethodHandles.lookup());
            }
        } catch (Throwable ignored) {
        }
        return MethodHandles.lookup();
    }
}
