package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Interface for configuring reflection operations with parameters.
 * <p>
 * All methods in this interface throw {@link IllegalStateException} if invoked
 * after a terminal operation has been called on the chain.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface ParameterStream<T> {
    /**
     * Specifies the name of the method to be accessed.
     * <p>
     * This is an intermediate operation.
     *
     * @param name the name of the method, which must not be null
     * @return this chain for further configuration
     * @throws NullPointerException if {@code name} is null
     */
    ParameterStream<T> name(@NotNull String name);

    /**
     * Sets whether private members should be made accessible.
     * <p>
     * This is an intermediate operation.
     *
     * @param accessible {@code true} to allow access to private members, {@code false} otherwise
     * @return this chain for further configuration
     */
    ParameterStream<T> accessible(boolean accessible);

    /**
     * Specifies the return type of the method.
     * <p>
     * This is an intermediate operation.
     *
     * @param returnType the return type, which must not be null
     * @return this chain for further configuration
     */
    ParameterStream<T> returnType(@NotNull Class<?> returnType);

    /**
     * Specifies the return type of the method.
     * <p>
     * This is an intermediate operation.
     *
     * @param returnType the fully qualified name of the return type, which must not be null
     * @return this chain for further configuration
     */
    ParameterStream<T> returnType(@NotNull String returnType);

    /**
     * Adds a single parameter type to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramType the parameter type, either a {@link Class} or a {@link String} class name
     * @return this chain for further configuration
     * @throws IllegalArgumentException if {@code paramType} is neither a {@link Class} nor a {@link String}
     */
    ParameterStream<T> param(@NotNull Object paramType);

    /**
     * Adds multiple parameter types to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramTypes the parameter types, each either a {@link Class} or a {@link String} class name
     * @return this chain for further configuration
     * @throws IllegalArgumentException if any element in {@code paramTypes} is neither a {@link Class} nor a {@link String}
     */
    ParameterStream<T> params(@NotNull Object... paramTypes);

    /**
     * Adds multiple parameter types to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramTypes the parameter types as {@link Class} objects
     * @return this chain for further configuration
     */
    ParameterStream<T> params(@NotNull Class<?>... paramTypes);

    /**
     * Adds multiple parameter types to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramTypes the parameter types as fully qualified class names
     * @return this chain for further configuration
     */
    ParameterStream<T> params(@NotNull String... paramTypes);

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
     * Retrieves a {@link MethodHandle} for a virtual method with the specified name and parameters.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle}
     * @throws IllegalStateException if the method name or class is not specified
     */
    MethodHandle virtualMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for a static method with the specified name and parameters.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle}
     * @throws IllegalStateException if the method name or class is not specified
     */
    MethodHandle staticMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for a constructor with the specified parameters.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle} for the constructor
     * @throws IllegalStateException if the class is not specified
     */
    MethodHandle constructorHandle();

    /**
     * Retrieves a public method with the specified name and no parameters, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Method} or the exception if not found
     */
    ReflectHolder<Method> findMethod();

    /**
     * Retrieves a declared method with the specified name and no parameters, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Method} or the exception if not found
     */
    ReflectHolder<Method> findDeclaredMethod();

    /**
     * Retrieves a constructor with no parameters, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Constructor} or the exception if not found
     */
    ReflectHolder<Constructor<T>> findConstructor();

    /**
     * Retrieves a {@link MethodHandle} for a virtual method, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<MethodHandle> findVirtualMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for a static method, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<MethodHandle> findStaticMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for a constructor, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<MethodHandle> findConstructorHandle();
}
