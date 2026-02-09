package io.wdsj.hybridfix.util.reflection;

import io.wdsj.hybridfix.HybridFix;
import net.lenni0451.reflect.JavaBypass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
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
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FluentReflect<T> {
    private FluentReflect() {
    }

    static final Method PRIVATE_LOOKUP_IN;
    static final MethodHandles.Lookup IMPL_LOOKUP;

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

    /**
     * Wraps an existing value into a ReflectHolder to start a functional chain.
     *
     * @param value the value to wrap
     * @param <T>   the type of the value
     * @return a successful ReflectHolder containing the value
     */
    public static <T> ReflectHolder<Object, T> ofHolder(T value) {
        return ReflectHolder.success(value);
    }
}