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
 * Constructor<String> constructor = ReflectionChain.fromClass(String.class)
 *     .params(byte[].class)
 *     .constructor(); // The type information will be kept
 * String str = constructor.newInstance(new byte[]{65, 66, 67});
 * </pre>
 * </p>
 *
 * @param <T> the type of the target class
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ReflectionChain<T> {
    private ReflectionChain() {
    }

    /**
     * Creates a new reflection chain for the specified class.
     *
     * @param <T>   the type of the target class
     * @param clazz the target class, which must not be null
     * @return a new reflection chain for the specified class
     */
    public static <T> IReflectionChain<T> fromClass(@NotNull Class<T> clazz) {
        Objects.requireNonNull(clazz, "The class must not be null");
        return new ReflectionChainImpl<>(clazz, null);
    }

    /**
     * Creates a new reflection chain for the class specified by its name.
     *
     * @param <T>       the type of the target class
     * @param className the full-qualified name of the target class, which must not be null
     * @return a new reflection chain for the specified class
     */
    public static <T> IReflectionChain<T> fromClass(@NotNull String className) {
        Objects.requireNonNull(className, "The class name must not be null");
        return new ReflectionChainImpl<>(null, className);
    }

    /**
     * Interface for configuring reflection operations without parameters.
     * <p>
     * All methods in this interface throw {@link IllegalStateException} if invoked
     * after a terminal operation has been called on the chain.
     */
    public interface IReflectionChain<T> {
        /**
         * Specifies the name of the method or field to be accessed.
         * <p>
         * This is an intermediate operation.
         *
         * @param name the name of the method or field, which must not be null
         * @return this chain for further configuration
         * @throws NullPointerException if {@code name} is null
         */
        IReflectionChain<T> name(@NotNull String name);

        /**
         * Sets whether private members should be made accessible.
         * <p>
         * This is an intermediate operation.
         *
         * @param accessible {@code true} to allow access to private members, {@code false} otherwise
         * @return this chain for further configuration
         */
        IReflectionChain<T> accessible(boolean accessible);

        /**
         * Adds a single parameter type to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramType the parameter type, either a {@link Class} or a {@link String} class name
         * @return a parameter chain for further configuration
         * @throws IllegalArgumentException if {@code paramType} is neither a {@link Class} nor a {@link String}
         */
        IParameterChain<T> param(@NotNull Object paramType);

        /**
         * Adds multiple parameter types to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramTypes the parameter types, each either a {@link Class} or a {@link String} class name
         * @return a parameter chain for further configuration
         * @throws IllegalArgumentException if any element in {@code paramTypes} is neither a {@link Class} nor a {@link String}
         */
        IParameterChain<T> params(@NotNull Object... paramTypes);

        /**
         * Adds multiple parameter types to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramTypes the parameter types as {@link Class} objects
         * @return a parameter chain for further configuration
         */
        IParameterChain<T> params(@NotNull Class<?>... paramTypes);

        /**
         * Adds multiple parameter types to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramTypes the parameter types as fully qualified class names
         * @return a parameter chain for further configuration
         */
        IParameterChain<T> params(@NotNull String... paramTypes);

        /**
         * Retrieves a public method with the specified name and no parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Method}
         * @throws IllegalStateException if the method name or class is not specified
         */
        Method method();

        /**
         * Retrieves a declared method with the specified name and no parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Method}
         * @throws IllegalStateException if the method name or class is not specified
         */
        Method declaredMethod();

        /**
         * Retrieves a declared field with the specified name.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Field}
         * @throws IllegalStateException if the field name or class is not specified
         */
        Field field();

        /**
         * Retrieves a constructor with no parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Constructor}
         * @throws IllegalStateException if the class is not specified
         */
        Constructor<T> constructor();

        /**
         * Retrieves a {@link MethodHandle} for a declared method with the specified name and no parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link MethodHandle}
         * @throws IllegalStateException if the method name or class is not specified
         */
        MethodHandle methodHandle();

        /**
         * Retrieves a {@link MethodHandle} for getting the value of a declared field.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link MethodHandle} for the field getter
         * @throws IllegalStateException if the field name or class is not specified
         */
        MethodHandle fieldGetter();

        /**
         * Retrieves a {@link MethodHandle} for setting the value of a declared field.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link MethodHandle} for the field setter
         * @throws IllegalStateException if the field name or class is not specified
         */
        MethodHandle fieldSetter();

        /**
         * Retrieves a {@link MethodHandle} for a constructor with no parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link MethodHandle} for the constructor
         * @throws IllegalStateException if the class is not specified
         */
        MethodHandle constructorHandle();
    }

    /**
     * Interface for configuring reflection operations with parameters.
     * <p>
     * All methods in this interface throw {@link IllegalStateException} if invoked
     * after a terminal operation has been called on the chain.
     */
    public interface IParameterChain<T> {
        /**
         * Specifies the name of the method to be accessed.
         * <p>
         * This is an intermediate operation.
         *
         * @param name the name of the method, which must not be null
         * @return this chain for further configuration
         * @throws NullPointerException if {@code name} is null
         */
        IParameterChain<T> name(@NotNull String name);

        /**
         * Sets whether private members should be made accessible.
         * <p>
         * This is an intermediate operation.
         *
         * @param accessible {@code true} to allow access to private members, {@code false} otherwise
         * @return this chain for further configuration
         */
        IParameterChain<T> accessible(boolean accessible);

        /**
         * Adds a single parameter type to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramType the parameter type, either a {@link Class} or a {@link String} class name
         * @return this chain for further configuration
         * @throws IllegalArgumentException if {@code paramType} is neither a {@link Class} nor a {@link String}
         */
        IParameterChain<T> param(@NotNull Object paramType);

        /**
         * Adds multiple parameter types to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramTypes the parameter types, each either a {@link Class} or a {@link String} class name
         * @return this chain for further configuration
         * @throws IllegalArgumentException if any element in {@code paramTypes} is neither a {@link Class} nor a {@link String}
         */
        IParameterChain<T> params(@NotNull Object... paramTypes);

        /**
         * Adds multiple parameter types to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramTypes the parameter types as {@link Class} objects
         * @return this chain for further configuration
         */
        IParameterChain<T> params(@NotNull Class<?>... paramTypes);

        /**
         * Adds multiple parameter types to the method or constructor signature.
         * <p>
         * This is an intermediate operation.
         *
         * @param paramTypes the parameter types as fully qualified class names
         * @return this chain for further configuration
         */
        IParameterChain<T> params(@NotNull String... paramTypes);

        /**
         * Retrieves a public method with the specified name and parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Method}
         * @throws IllegalStateException if the method name or class is not specified
         */
        Method method();

        /**
         * Retrieves a declared method with the specified name and parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Method}
         * @throws IllegalStateException if the method name or class is not specified
         */
        Method declaredMethod();

        /**
         * Retrieves a constructor with the specified parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link Constructor}
         * @throws IllegalStateException if the class is not specified
         */
        Constructor<T> constructor();

        /**
         * Retrieves a {@link MethodHandle} for a declared method with the specified name and parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link MethodHandle}
         * @throws IllegalStateException if the method name or class is not specified
         */
        MethodHandle methodHandle();

        /**
         * Retrieves a {@link MethodHandle} for a constructor with the specified parameters.
         * <p>
         * This is a terminal operation.
         *
         * @return the matching {@link MethodHandle} for the constructor
         * @throws IllegalStateException if the class is not specified
         */
        MethodHandle constructorHandle();
    }

    private static class ReflectionChainImpl<T> implements IReflectionChain<T> {
        private final Class<T> targetClass;
        private final String targetClassName;
        private String name;
        private boolean isAccessible = false;
        private boolean isTerminated = false;

        ReflectionChainImpl(Class<T> clazz, String className) {
            this.targetClass = clazz;
            this.targetClassName = className;
        }

        private void checkNotTerminated() {
            if (isTerminated) {
                throw new IllegalStateException("Chain has been terminated by a terminal operation and cannot be modified");
            }
        }

        @Override
        public IReflectionChain<T> name(@NotNull String name) {
            checkNotTerminated();
            Objects.requireNonNull(name, "Name cannot be null");
            this.name = name;
            return this;
        }

        @Override
        public IReflectionChain<T> accessible(boolean accessible) {
            checkNotTerminated();
            this.isAccessible = accessible;
            return this;
        }

        @Override
        public IParameterChain<T> param(@NotNull Object paramType) {
            checkNotTerminated();
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, targetClassName, name, isAccessible);
            chain.param(paramType);
            markTerminated();
            return chain;
        }

        @Override
        public IParameterChain<T> params(Object... paramTypes) {
            checkNotTerminated();
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, targetClassName, name, isAccessible);
            chain.params(paramTypes);
            markTerminated();
            return chain;
        }

        @Override
        public IParameterChain<T> params(Class<?>... paramTypes) {
            checkNotTerminated();
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, targetClassName, name, isAccessible);
            chain.params(paramTypes);
            markTerminated();
            return chain;
        }

        @Override
        public IParameterChain<T> params(String... paramTypes) {
            checkNotTerminated();
            ParameterChainImpl<T> chain = new ParameterChainImpl<>(targetClass, targetClassName, name, isAccessible);
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
                Class<T> clazz = (Class<T>) Class.forName(targetClassName);
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
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Method result = resolveTargetClass().getMethod(name);
                markTerminated();
                return result;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Method declaredMethod() {
            checkNotTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Method method = resolveTargetClass().getDeclaredMethod(name);
                if (isAccessible) method.setAccessible(true);
                markTerminated();
                return method;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Field field() {
            checkNotTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Field name must be specified");
                }
                Field field = resolveTargetClass().getDeclaredField(name);
                if (isAccessible) field.setAccessible(true);
                markTerminated();
                return field;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Constructor<T> constructor() {
            checkNotTerminated();
            try {
                Constructor<T> constructor = resolveTargetClass().getDeclaredConstructor();
                if (isAccessible) constructor.setAccessible(true);
                markTerminated();
                return constructor;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle methodHandle() {
            checkNotTerminated();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(declaredMethod());
                markTerminated();
                return handle;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle fieldGetter() {
            checkNotTerminated();
            try {
                MethodHandle getter = MethodHandles.lookup().unreflectGetter(field());
                markTerminated();
                return getter;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle fieldSetter() {
            checkNotTerminated();
            try {
                MethodHandle setter = MethodHandles.lookup().unreflectSetter(field());
                markTerminated();
                return setter;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle constructorHandle() {
            checkNotTerminated();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflectConstructor(constructor());
                markTerminated();
                return handle;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }
    }

    private static class ParameterChainImpl<T> implements IParameterChain<T> {
        private final Class<T> targetClass;
        private final String targetClassName;
        private String name;
        private final List<Object> parameterTypes = new ArrayList<>();
        private boolean isAccessible;
        private boolean isTerminated = false;

        ParameterChainImpl(Class<T> targetClass, String targetClassName, String name, boolean isAccessible) {
            this.targetClass = targetClass;
            this.targetClassName = targetClassName;
            this.name = name;
            this.isAccessible = isAccessible;
        }

        private void checkNotTerminated() {
            if (this.isTerminated) {
                throw new IllegalStateException("Chain has been terminated by a terminal operation and cannot be modified");
            }
        }

        @Override
        public IParameterChain<T> name(@NotNull String name) {
            checkNotTerminated();
            Objects.requireNonNull(name, "Name cannot be null");
            this.name = name;
            return this;
        }

        @Override
        public IParameterChain<T> accessible(boolean accessible) {
            checkNotTerminated();
            this.isAccessible = accessible;
            return this;
        }

        @Override
        public IParameterChain<T> param(@NotNull Object paramType) {
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
        public IParameterChain<T> params(Object... paramTypes) {
            checkNotTerminated();
            for (Object paramType : paramTypes) {
                param(paramType);
            }
            return this;
        }

        @Override
        public IParameterChain<T> params(Class<?>... paramTypes) {
            checkNotTerminated();
            this.parameterTypes.addAll(Arrays.asList(paramTypes));
            return this;
        }

        @Override
        public IParameterChain<T> params(String... paramTypes) {
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
                Class<T> clazz = (Class<T>) Class.forName(targetClassName);
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
                        resolvedTypes.add(Class.forName((String) paramType));
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
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Method result = resolveTargetClass().getMethod(name, resolveParameterTypes());
                markTerminated();
                return result;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Method declaredMethod() {
            checkNotTerminated();
            try {
                if (name == null) {
                    throw new IllegalStateException("Method name must be specified");
                }
                Method method = resolveTargetClass().getDeclaredMethod(name, resolveParameterTypes());
                if (isAccessible) method.setAccessible(true);
                markTerminated();
                return method;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public Constructor<T> constructor() {
            checkNotTerminated();
            try {
                Constructor<T> constructor = resolveTargetClass().getDeclaredConstructor(resolveParameterTypes());
                if (isAccessible) constructor.setAccessible(true);
                markTerminated();
                return constructor;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle methodHandle() {
            checkNotTerminated();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(declaredMethod());
                markTerminated();
                return handle;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }

        @Override
        public MethodHandle constructorHandle() {
            checkNotTerminated();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflectConstructor(constructor());
                markTerminated();
                return handle;
            } catch (Exception e) {
                SneakyThrow.throw0(e);
                throw new RuntimeException(e); // unreachable
            }
        }
    }

    private static class SneakyThrow {
        private SneakyThrow() {}
        @SuppressWarnings("unchecked")
        public static <T extends Throwable> void throw0(Throwable t) throws T {
            throw (T) t;
        }
    }
}