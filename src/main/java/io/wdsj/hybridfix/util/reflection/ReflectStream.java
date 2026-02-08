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
     * Specifies the type to which the result  should be cast.
     * <p>
     * This is an intermediate operation.
     *
     * @param type the type to which the result should be cast, which must not be null
     * @return this chain for further configuration
     */
    <U> ReflectStream<U> as(@NotNull Class<U> type);

    /**
     * Sets whether the target class should be initialized during resolution.
     * <p>
     * If set to {@code true}, the class will be initialized (running its static
     * initializers) when it is resolved by a terminal operation.
     * Default is {@code false}.
     * </p>
     * <p>
     * This is an intermediate operation.
     *
     * @param initialize whether to initialize the class
     * @return this chain for further configuration
     */
    ReflectStream<T> initialize(boolean initialize);

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
     * Resolves the target class and returns it.
     * <p>
     * This is a terminal operation.
     *
     * @return the resolved {@link Class} object
     */
    Class<T> type();

    /**
     * Resolves the target class and returns a holder containing the result.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the class or the failure cause
     */
    ReflectHolder<T, Class<T>> findType();

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

    /**
     * Retrieves a public method with the specified name and no parameters, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Method} or the exception if not found
     */
    ReflectHolder<T, Method> findMethod();

    /**
     * Retrieves a declared method with the specified name and no parameters, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Method} or the exception if not found
     */
    ReflectHolder<T, Method> findDeclaredMethod();

    /**
     * Retrieves a field with the specified name, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Field} or the exception if not found
     */
    ReflectHolder<T, Field> findField();

    /**
     * Retrieves a declared field with the specified name, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Field} or the exception if not found
     */
    ReflectHolder<T, Field> findDeclaredField();

    /**
     * Retrieves a constructor with no parameters, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link Constructor} or the exception if not found
     */
    ReflectHolder<T, Constructor<T>> findConstructor();

    /**
     * Retrieves a {@link MethodHandle} for a virtual method, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findVirtualMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for a static method, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findStaticMethodHandle();

    /**
     * Retrieves a {@link MethodHandle} for getting the value of a virtual field, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching getter {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findVirtualFieldGetter();

    /**
     * Retrieves a {@link MethodHandle} for getting the value of a static field, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching getter {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findStaticFieldGetter();

    /**
     * Retrieves a {@link MethodHandle} for setting the value of a virtual field, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching setter {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findVirtualFieldSetter();

    /**
     * Retrieves a {@link MethodHandle} for setting the value of a static field, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching setter {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findStaticFieldSetter();

    /**
     * Retrieves a {@link MethodHandle} for a constructor, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link MethodHandle} or the exception if not found
     */
    ReflectHolder<T, MethodHandle> findConstructorHandle();

    /**
     * Retrieves an unsafe field accessor for a virtual field, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link UnsafeFieldAccessor} or the exception if not found
     */
    ReflectHolder<T, UnsafeFieldAccessor> findVirtualFieldAccessor();

    /**
     * Retrieves an unsafe field accessor for a static field, wrapped in a {@link ReflectHolder}.
     * <p>
     * This is a terminal operation.
     *
     * @return a {@link ReflectHolder} containing the matching {@link UnsafeFieldAccessor} or the exception if not found
     */
    ReflectHolder<T, UnsafeFieldAccessor> findStaticFieldAccessor();
}
