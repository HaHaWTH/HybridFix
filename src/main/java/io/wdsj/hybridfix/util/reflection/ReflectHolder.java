package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A container for reflection members or the results of reflection operations.
 * <p>
 * This holder allows for safe chaining of reflection tasks.
 * It tracks both the resulting value (or reflection member) and any potential {@link Throwable}
 * that occurred during the lookup or execution phase.
 * </p>
 *
 * @param <M> the type of the target class (the owner of the reflection member)
 * @param <T> the type of the contained value (a {@link Method}, {@link Field}, or an execution result)
 */
@SuppressWarnings({"unused", "unchecked"})
public final class ReflectHolder<M, T> {
    private final T value;
    private final Throwable exception;
    private boolean verified = false;
    private final boolean isFailure;

    private ReflectHolder(T value, Throwable exception, boolean isFailure) {
        this.value = value;
        this.exception = exception;
        this.isFailure = isFailure;
    }

    static <M, T> ReflectHolder<M, T> success(T value) {
        return new ReflectHolder<>(value, null, false);
    }

    static <M, T> ReflectHolder<M, T> failure(@NotNull Throwable exception) {
        return new ReflectHolder<>(null, exception, true);
    }

    /**
     * Checks if a value is present in this holder.
     *
     * @return {@code true} if the holder contains a value, {@code false} otherwise
     */
    public boolean isPresent() {
        return !isFailure;
    }

    /**
     * Retrieves the contained value.
     *
     * @return the value
     * @throws RuntimeException (SneakyThrow) if this holder represents a failure
     */
    public T get() {
        if (isPresent()) return value;
        if (exception == null) {
            throw new NoSuchElementException("No value or exception present in ReflectHolder");
        }
        SneakyThrow.throw0(exception);
        return null; // unreachable
    }

    /**
     * Ensures the contained value is of the expected type.
     * <p>
     * Type checking is performed only once and cached for performance.
     * </p>
     *
     * @param expectedTypes valid classes the value should be an instance of
     * @return the contained value cast to {@code T}
     * @throws IllegalStateException if the type does not match
     */
    private T ensureType(Class<?>... expectedTypes) {
        if (verified) return value;

        T val = get();
        if (val == null) {
            throw new IllegalStateException("ReflectHolder contains null, but expected a reflection member");
        }
        boolean match = false;
        for (Class<?> type : expectedTypes) {
            if (type.isInstance(val)) {
                match = true;
                break;
            }
        }

        if (!match) {
            StringBuilder sb = new StringBuilder("ReflectHolder contains ")
                    .append(val.getClass().getName())
                    .append(", but expected one of: ");
            for (int i = 0; i < expectedTypes.length; i++) {
                sb.append(expectedTypes[i].getSimpleName());
                if (i < expectedTypes.length - 1) sb.append(", ");
            }
            throw new IllegalStateException(sb.toString());
        }

        verified = true;
        return val;
    }

    /**
     * Returns the contained value if present, otherwise returns {@code other}.
     */
    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    /**
     * Returns the contained value if present, otherwise invokes {@code other} and returns its result.
     */
    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? value : other.get();
    }

    /**
     * Executes the given action if a value is present.
     *
     * @param action the task to execute
     * @return this holder
     */
    public ReflectHolder<M, T> ifPresent(ThrowingConsumer<? super T> action) {
        try {
            if (isPresent()) action.accept(value);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
        return this;
    }

    /**
     * Transforms the contained value using the provided mapper.
     * <p>
     * If this holder is a failure, the failure is propagated.
     * </p>
     *
     * @param mapper the transformation function, which returns the transformed value
     * @param <U> the type of the new value
     * @return a new ReflectHolder containing the mapped value
     */
    public <U> ReflectHolder<M, U> map(ThrowingFunction<? super T, ? extends U> mapper) {
        if (!isPresent()) return failure(exception);
        try {
            U result = mapper.apply(value);
            return success(result);
        } catch (Throwable t) {
            return failure(t);
        }
    }

    /**
     * Retrieves the exception that caused the failure, if any.
     */
    @Nullable
    public Throwable getException() {
        return exception;
    }

    /**
     * Converts this holder to a standard {@link Optional}.
     * <p>
     * Note: This operation loses the exception information.
     * </p>
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * Creates a new instance using the contained constructor.
     */
    @NotNull
    public M newInstance(Object... args) {
        Constructor<M> c = (Constructor<M>) ensureType(Constructor.class);
        try {
            return c.newInstance(args);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

    /**
     * Invokes the contained method.
     */
    @NotNull
    public <R> R invoke(@Nullable Object obj, Object... args) {
        Method m = (Method) ensureType(Method.class);
        try {
            return (R) m.invoke(obj, args);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

    /**
     * Invokes the contained MethodHandle.
     */
    @NotNull
    public <R> R invokeHandle(Object... args) {
        MethodHandle mh = (MethodHandle) ensureType(MethodHandle.class);
        try {
            return (R) mh.invokeWithArguments(args);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

    /**
     * Executes the consumer if this holder represents a failure.
     */
    public ReflectHolder<M, T> ifFailure(Consumer<Throwable> consumer) {
        if (!isPresent()) {
            consumer.accept(exception);
        }
        return this;
    }

    /**
     * Safely accepts a task using the contained value.
     * <p>
     * Any exception thrown during the task will be caught and passed to the fallback consumer.
     * </p>
     */
    public void accept(@NotNull ThrowingConsumer<T> task, @NotNull Consumer<@NotNull Throwable> fallback) {
        try {
            task.accept(get());
        } catch (Throwable t) {
            fallback.accept(t);
        }
    }

    /**
     * Retrieves the value of the contained field.
     */
    public <V> V get(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) return (V) ((Field) val).get(obj);
            return ((UnsafeFieldAccessor) val).get(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

    /**
     * Sets the value of the contained field.
     */
    public void set(@Nullable Object obj, Object value) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) {
                ((Field) val).set(obj, value);
            } else {
                ((UnsafeFieldAccessor) val).set(obj, value);
            }
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public int getInt(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getInt(obj) : ((UnsafeFieldAccessor) val).getInt(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return 0;
        }
    }

    public void setInt(@Nullable Object obj, int v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setInt(obj, v);
            else ((UnsafeFieldAccessor) val).setInt(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public long getLong(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getLong(obj) : ((UnsafeFieldAccessor) val).getLong(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return 0L;
        }
    }

    public void setLong(@Nullable Object obj, long v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setLong(obj, v);
            else ((UnsafeFieldAccessor) val).setLong(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public boolean getBoolean(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getBoolean(obj) : ((UnsafeFieldAccessor) val).getBoolean(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return false;
        }
    }

    public void setBoolean(@Nullable Object obj, boolean v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setBoolean(obj, v);
            else ((UnsafeFieldAccessor) val).setBoolean(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public double getDouble(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getDouble(obj) : ((UnsafeFieldAccessor) val).getDouble(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return 0.0;
        }
    }

    public void setDouble(@Nullable Object obj, double v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setDouble(obj, v);
            else ((UnsafeFieldAccessor) val).setDouble(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public float getFloat(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getFloat(obj) : ((UnsafeFieldAccessor) val).getFloat(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return 0.0f;
        }
    }

    public void setFloat(@Nullable Object obj, float v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setFloat(obj, v);
            else ((UnsafeFieldAccessor) val).setFloat(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public byte getByte(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getByte(obj) : ((UnsafeFieldAccessor) val).getByte(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return 0;
        }
    }

    public void setByte(@Nullable Object obj, byte v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setByte(obj, v);
            else ((UnsafeFieldAccessor) val).setByte(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public short getShort(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getShort(obj) : ((UnsafeFieldAccessor) val).getShort(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return 0;
        }
    }

    public void setShort(@Nullable Object obj, short v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setShort(obj, v);
            else ((UnsafeFieldAccessor) val).setShort(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    public char getChar(@Nullable Object obj) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            return (val instanceof Field) ? ((Field) val).getChar(obj) : ((UnsafeFieldAccessor) val).getChar(obj);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return '\u0000';
        }
    }

    public void setChar(@Nullable Object obj, char v) {
        T val = ensureType(Field.class, UnsafeFieldAccessor.class);
        try {
            if (val instanceof Field) ((Field) val).setChar(obj, v);
            else ((UnsafeFieldAccessor) val).setChar(obj, v);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Throwable;
    }
}