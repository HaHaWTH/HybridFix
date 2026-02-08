package io.wdsj.hybridfix.util.reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "unchecked"})
public final class ReflectHolder<M, T> {
    private final T value;
    private final Throwable exception;

    private ReflectHolder(T value, Throwable exception) {
        this.value = value;
        this.exception = exception;
    }

    static <M, T> ReflectHolder<M, T> success(@NotNull T value) {
        return new ReflectHolder<>(value, null);
    }

    static <M, T> ReflectHolder<M, T> failure(@NotNull Throwable exception) {
        return new ReflectHolder<>(null, exception);
    }

    public boolean isPresent() {
        return value != null;
    }

    @NotNull
    public T get() {
        if (value != null) return value;
        SneakyThrow.throw0(exception);
        return null; // unreachable
    }

    private T ensureType(Class<?>... expectedTypes) {
        T val = get();
        for (Class<?> type : expectedTypes) {
            if (type.isInstance(val)) return val;
        }
        StringBuilder sb = new StringBuilder("ReflectHolder contains ")
                .append(val.getClass().getName())
                .append(", but expected one of: ");
        for (int i = 0; i < expectedTypes.length; i++) {
            sb.append(expectedTypes[i].getSimpleName());
            if (i < expectedTypes.length - 1) sb.append(", ");
        }
        throw new IllegalStateException(sb.toString());
    }

    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? value : other.get();
    }

    public ReflectHolder<M, T> ifPresent(Consumer<? super T> action) {
        if (isPresent()) action.accept(value);
        return this;
    }

    public <U> ReflectHolder<M, U> map(Function<? super T, ? extends U> mapper) {
        if (!isPresent()) return ReflectHolder.failure(exception);
        try {
            return ReflectHolder.success(mapper.apply(value));
        } catch (Throwable t) {
            return ReflectHolder.failure(t);
        }
    }

    @Nullable
    public Throwable getException() {
        return exception;
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    public M newInstance(Object... args) {
        Constructor<M> c = (Constructor<M>) ensureType(Constructor.class);
        try {
            return c.newInstance(args);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

    public <R> R invoke(@Nullable Object obj, Object... args) {
        Method m = (Method) ensureType(Method.class);
        try {
            return (R) m.invoke(obj, args);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

    public <R> R invokeHandle(Object... args) {
        MethodHandle mh = (MethodHandle) ensureType(MethodHandle.class);
        try {
            return (R) mh.invokeWithArguments(args);
        } catch (Throwable t) {
            SneakyThrow.throw0(t);
            return null;
        }
    }

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
}