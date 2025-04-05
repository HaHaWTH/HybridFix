package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fluent API for reflection operations on a target class.
 * <p>
 * This class provides a chainable interface to access methods, fields, constructors,
 * and their corresponding {@link MethodHandle}s of a specified class. It supports
 * both {@link Class} and {@link String} representations for class and parameter types,
 * and allows toggling accessibility for private members. All reflection operations
 * throw {@link RuntimeException} on failure for simpler error handling.
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
 * @param <T> the type of the target class
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ReflectionChain<T> {
    private ReflectionChain() {
    }

    public static <T> IReflectionChain<T> fromClass(@NotNull Class<T> clazz) {
        return new ReflectionChainImpl<>(clazz);
    }

    public static <T> IReflectionChain<T> fromClass(@NotNull String className) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) Class.forName(className);
            return new ReflectionChainImpl<>(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public interface IReflectionChain<T> {
        IReflectionChain<T> name(@NotNull String name);

        IReflectionChain<T> accessible(boolean accessible);

        IParameterChain<T> param(Object paramType);

        IParameterChain<T> params(Object... paramTypes);

        IParameterChain<T> params(Class<?>... paramTypes);

        IParameterChain<T> params(String... paramTypes);

        Method method();

        Method methodDeclared();

        Field field();

        Constructor<T> constructor();

        MethodHandle methodHandle();

        MethodHandle fieldGetter();

        MethodHandle fieldSetter();

        MethodHandle constructorHandle();
    }

    public interface IParameterChain<T> {
        IParameterChain<T> name(@NotNull String name);

        IParameterChain<T> accessible(boolean accessible);

        IParameterChain<T> param(Object paramType);

        IParameterChain<T> params(Object... paramTypes);

        IParameterChain<T> params(Class<?>... paramTypes);

        IParameterChain<T> params(String... paramTypes);

        Method method();

        Method methodDeclared();

        Constructor<T> constructor();

        MethodHandle methodHandle();

        MethodHandle constructorHandle();
    }

    private static class ReflectionChainImpl<T> implements IReflectionChain<T> {
        private final Class<T> targetClass;
        private String name;
        private boolean isAccessible = false;

        ReflectionChainImpl(Class<T> clazz) {
            this.targetClass = clazz;
        }

        @Override
        public IReflectionChain<T> name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public IReflectionChain<T> accessible(boolean accessible) {
            this.isAccessible = accessible;
            return this;
        }

        @Override
        public IParameterChain<T> param(Object paramType) {
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, name, isAccessible);
            chain.param(paramType);
            return chain;
        }

        @Override
        public IParameterChain<T> params(Object... paramTypes) {
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, name, isAccessible);
            chain.params(paramTypes);
            return chain;
        }

        @Override
        public IParameterChain<T> params(Class<?>... paramTypes) {
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, name, isAccessible);
            chain.params(paramTypes);
            return chain;
        }

        @Override
        public IParameterChain<T> params(String... paramTypes) {
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, name, isAccessible);
            chain.params(paramTypes);
            return chain;
        }

        @Override
        public Method method() {
            try {
                if (targetClass == null || name == null) {
                    throw new IllegalStateException("Class and method name must be specified");
                }
                return targetClass.getMethod(name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Method methodDeclared() {
            try {
                if (targetClass == null || name == null) {
                    throw new IllegalStateException("Class and method name must be specified");
                }
                Method method = targetClass.getDeclaredMethod(name);
                if (isAccessible) method.setAccessible(true);
                return method;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Field field() {
            try {
                if (targetClass == null || name == null) {
                    throw new IllegalStateException("Class and field name must be specified");
                }
                Field field = targetClass.getDeclaredField(name);
                if (isAccessible) field.setAccessible(true);
                return field;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Constructor<T> constructor() {
            try {
                if (targetClass == null) {
                    throw new IllegalStateException("Class must be specified");
                }
                Constructor<T> constructor = targetClass.getDeclaredConstructor();
                if (isAccessible) constructor.setAccessible(true);
                return constructor;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle methodHandle() {
            try {
                return MethodHandles.lookup().unreflect(methodDeclared());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle fieldGetter() {
            try {
                return MethodHandles.lookup().unreflectGetter(field());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle fieldSetter() {
            try {
                return MethodHandles.lookup().unreflectSetter(field());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle constructorHandle() {
            try {
                return MethodHandles.lookup().unreflectConstructor(constructor());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ParameterChainImpl<T> implements IParameterChain<T> {
        private final Class<T> targetClass;
        private String name;
        private final List<Class<?>> parameterTypes = new ArrayList<>();
        private boolean isAccessible;

        ParameterChainImpl(Class<T> targetClass, String name, boolean isAccessible) {
            this.targetClass = targetClass;
            this.name = name;
            this.isAccessible = isAccessible;
        }

        @Override
        public IParameterChain<T> name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public IParameterChain<T> accessible(boolean accessible) {
            this.isAccessible = accessible;
            return this;
        }

        @Override
        public IParameterChain<T> param(Object paramType) {
            if (paramType instanceof Class<?>) {
                this.parameterTypes.add((Class<?>) paramType);
            } else if (paramType instanceof String) {
                try {
                    this.parameterTypes.add(Class.forName((String) paramType));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalArgumentException("Parameter must be Class or String");
            }
            return this;
        }

        @Override
        public IParameterChain<T> params(Object... paramTypes) {
            for (Object paramType : paramTypes) {
                param(paramType);
            }
            return this;
        }

        @Override
        public IParameterChain<T> params(Class<?>... paramTypes) {
            this.parameterTypes.addAll(Arrays.asList(paramTypes));
            return this;
        }

        @Override
        public IParameterChain<T> params(String... paramTypes) {
            for (String paramType : paramTypes) {
                try {
                    this.parameterTypes.add(Class.forName(paramType));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return this;
        }

        @Override
        public Method method() {
            try {
                if (targetClass == null || name == null) {
                    throw new IllegalStateException("Class and method name must be specified");
                }
                return targetClass.getMethod(name, parameterTypes.toArray(new Class[0]));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Method methodDeclared() {
            try {
                if (targetClass == null || name == null) {
                    throw new IllegalStateException("Class and method name must be specified");
                }
                Method method = targetClass.getDeclaredMethod(name, parameterTypes.toArray(new Class[0]));
                if (isAccessible) method.setAccessible(true);
                return method;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Constructor<T> constructor() {
            try {
                if (targetClass == null) {
                    throw new IllegalStateException("Class must be specified");
                }
                Constructor<T> constructor = targetClass.getDeclaredConstructor(parameterTypes.toArray(new Class[0]));
                if (isAccessible) constructor.setAccessible(true);
                return constructor;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle methodHandle() {
            try {
                return MethodHandles.lookup().unreflect(methodDeclared());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle constructorHandle() {
            try {
                return MethodHandles.lookup().unreflectConstructor(constructor());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}