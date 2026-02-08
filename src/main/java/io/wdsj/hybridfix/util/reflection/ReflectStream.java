package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Interface for configuring reflection operations without parameters.
 * <p>
 * All methods in this interface throw {@link IllegalStateException} if invoked
 * after a terminal operation has been called on the chain.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface ReflectStream<T> {
    /**
     * Specifies the name of the method or field to be accessed.
     * <p>
     * This is an intermediate operation.
     *
     * @param name the name of the method or field, which must not be null
     * @return this chain for further configuration
     * @throws NullPointerException if {@code name} is null
     */
    ReflectStream<T> name(@NotNull String name);

    /**
     * Sets whether private members should be made accessible.
     * <p>
     * This is an intermediate operation.
     *
     * @param accessible {@code true} to allow access to private members, {@code false} otherwise
     * @return this chain for further configuration
     */
    ReflectStream<T> accessible(boolean accessible);

    /**
     * Specifies the return type of the method or the type of the field.
     * <p>
     * This is an intermediate operation.
     *
     * @param returnType the return type or field type, which must not be null
     * @return this chain for further configuration
     */
    ReflectStream<T> returnType(@NotNull Class<?> returnType);

    /**
     * Specifies the return type of the method or the type of the field.
     * <p>
     * This is an intermediate operation.
     *
     * @param returnType the fully qualified name of the return type, which must not be null
     * @return this chain for further configuration
     */
    ReflectStream<T> returnType(@NotNull String returnType);

    /**
     * Adds a single parameter type to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramType the parameter type, either a {@link Class} or a {@link String} class name
     * @return a parameter chain for further configuration
     * @throws IllegalArgumentException if {@code paramType} is neither a {@link Class} nor a {@link String}
     */
    ParameterStream<T> param(@NotNull Object paramType);

    /**
     * Adds multiple parameter types to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramTypes the parameter types, each either a {@link Class} or a {@link String} class name
     * @return a parameter chain for further configuration
     * @throws IllegalArgumentException if any element in {@code paramTypes} is neither a {@link Class} nor a {@link String}
     */
    ParameterStream<T> params(@NotNull Object... paramTypes);

    /**
     * Adds multiple parameter types to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramTypes the parameter types as {@link Class} objects
     * @return a parameter chain for further configuration
     */
    ParameterStream<T> params(@NotNull Class<?>... paramTypes);

    /**
     * Adds multiple parameter types to the method or constructor signature.
     * <p>
     * This is an intermediate operation.
     *
     * @param paramTypes the parameter types as fully qualified class names
     * @return a parameter chain for further configuration
     */
    ParameterStream<T> params(@NotNull String... paramTypes);

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
     * Retrieves a field with the specified name.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link Field}
     * @throws IllegalStateException if the field name or class is not specified
     */
    Field field();

    /**
     * Retrieves a declared field with the specified name.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link Field}
     * @throws IllegalStateException if the field name or class is not specified
     */
    Field declaredField();

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
     * Retrieves a {@link MethodHandle} for a virtual method with the specified name and no parameters.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle}
     * @throws IllegalStateException if the method name or class is not specified
     */
    MethodHandle virtualMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for a static method with the specified name and no parameters.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle}
     * @throws IllegalStateException if the method name or class is not specified
     */
    MethodHandle staticMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for getting the value of a virtual field.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle} for the field getter
     * @throws IllegalStateException if the field name or class is not specified
     */
    MethodHandle virtualFieldGetter();

    /**
     * Retrieves a {@link MethodHandle} for getting the value of a static field.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle} for the field getter
     * @throws IllegalStateException if the field name or class is not specified
     */
    MethodHandle staticFieldGetter();

    /**
     * Retrieves a {@link MethodHandle} for setting the value of a virtual field.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle} for the field setter
     * @throws IllegalStateException if the field name or class is not specified
     */
    MethodHandle virtualFieldSetter();

    /**
     * Retrieves a {@link MethodHandle} for setting the value of a static field.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle} for the field setter
     * @throws IllegalStateException if the field name or class is not specified
     */
    MethodHandle staticFieldSetter();

    /**
     * Retrieves a {@link MethodHandle} for a constructor with no parameters.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link MethodHandle} for the constructor
     * @throws IllegalStateException if the class is not specified
     */
    MethodHandle constructorHandle();

    /**
     * Retrieves an unsafe field accessor for a virtual field.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link UnsafeFieldAccessor} for the field
     * @throws IllegalStateException if the field name or class is not specified
     */
    UnsafeFieldAccessor virtualFieldAccessor();

    /**
     * Retrieves an unsafe field accessor for a static field.
     * <p>
     * This is a terminal operation.
     *
     * @return the matching {@link UnsafeFieldAccessor} for the field
     * @throws IllegalStateException if the field name or class is not specified
     */
    UnsafeFieldAccessor staticFieldAccessor();
}
