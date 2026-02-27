package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue", "unchecked"})
class ReflectStreamImpl<T> implements ReflectStream<T> {
    private final Class<T> targetClass;
    private final String targetClassName;
    private final ClassLoader classLoader;
    private String name;
    private boolean isAccessible = false;
    private boolean isTerminated = false;
    private Object returnType = void.class;
    private boolean initialize = false;

    private Class<?> typeContract;

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
                        ? Class.forName((String) returnType, initialize, classLoader)
                        : Class.forName((String) returnType, initialize, getClass().getClassLoader());
            } catch (Throwable t) {
                SneakyThrow.throw0(t);
                throw new RuntimeException(t); // unreachable
            }
        } else {
            throw new IllegalStateException("Invalid return type: " + returnType);
        }
    }

    @Override
    public <U> ReflectStream<U> as(@NotNull Class<U> type) {
        checkNotTerminated();
        this.typeContract = type;
        return (ReflectStream<U>) this;
    }

    @Override
    public ReflectStream<T> initialize(boolean initialize) {
        checkNotTerminated();
        this.initialize = initialize;
        return this;
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
        if (typeContract != null) {
            chain.as(typeContract);
        }
        chain.initialize(initialize);
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
        if (typeContract != null) {
            chain.as(typeContract);
        }
        chain.initialize(initialize);
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
        if (typeContract != null) {
            chain.as(typeContract);
        }
        chain.initialize(initialize);
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
        if (typeContract != null) {
            chain.as(typeContract);
        }
        chain.initialize(initialize);
        chain.params(paramTypes);
        markTerminated();
        return chain;
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

    private void markTerminated() {
        isTerminated = true;
    }

    private void checkTypeContract(Class<T> clazz) {
        if (typeContract != null && !typeContract.isAssignableFrom(clazz)) {
            throw new ClassCastException("Class " + clazz.getName() +
                    " does not match type contract: " + typeContract.getName());
        }
    }

    @Override
    public Class<T> type() {
        checkNotTerminated();
        markTerminated();
        return resolveTargetClass();
    }

    @Override
    public ReflectHolder<T, Class<T>> findType() {
        checkNotTerminated();
        markTerminated();
        return findTerminal(this::resolveTargetClass);
    }

    @Override
    public Method method() {
        checkNotTerminated();
        markTerminated();
        try {
            if (name == null) {
                throw new IllegalStateException("Method name must be specified");
            }
            Method method = resolveTargetClass().getMethod(name);
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
            Method method = resolveTargetClass().getDeclaredMethod(name);
            if (isAccessible) method.setAccessible(true);
            return method;
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
            Constructor<T> constructor = resolveTargetClass().getDeclaredConstructor();
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
            return getLookup(targetClazz).findVirtual(targetClazz, name, MethodType.methodType(resolveReturnType()));
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
            return getLookup(targetClazz).findStatic(targetClazz, name, MethodType.methodType(resolveReturnType()));
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
            return getLookup(targetClazz).findConstructor(targetClazz, MethodType.methodType(void.class));
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            throw new RuntimeException(t); // unreachable
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
    public ReflectHolder<T, Field> findField() {
        return findTerminal(this::field);
    }

    @Override
    public ReflectHolder<T, Field> findDeclaredField() {
        return findTerminal(this::declaredField);
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
    public ReflectHolder<T, MethodHandle> findVirtualFieldGetter() {
        return findTerminal(this::virtualFieldGetter);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findStaticFieldGetter() {
        return findTerminal(this::staticFieldGetter);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findVirtualFieldSetter() {
        return findTerminal(this::virtualFieldSetter);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findStaticFieldSetter() {
        return findTerminal(this::staticFieldSetter);
    }

    @Override
    public ReflectHolder<T, MethodHandle> findConstructorHandle() {
        return findTerminal(this::constructorHandle);
    }

    @Override
    public ReflectHolder<T, UnsafeFieldAccessor> findVirtualFieldAccessor() {
        return findTerminal(this::virtualFieldAccessor);
    }

    @Override
    public ReflectHolder<T, UnsafeFieldAccessor> findStaticFieldAccessor() {
        return findTerminal(this::staticFieldAccessor);
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
