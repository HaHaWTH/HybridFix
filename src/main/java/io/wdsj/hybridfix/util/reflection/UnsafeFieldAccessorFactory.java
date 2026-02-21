package io.wdsj.hybridfix.util.reflection;

import net.lenni0451.reflect.JavaBypass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings({"unused", "unchecked", "removal"})
public final class UnsafeFieldAccessorFactory {
    static final sun.misc.Unsafe UNSAFE = JavaBypass.UNSAFE;

    static UnsafeFieldAccessor create(Field field, boolean isStaticExpected) {
        if (UNSAFE == null) throw new UnsupportedOperationException("Unsafe not available");
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (isStatic != isStaticExpected) {
            throw new IllegalArgumentException("Field " + field.getName() + " is " + (isStatic ? "static" : "instance") + " but " + (isStaticExpected ? "static" : "instance") + " expected");
        }
        Class<?> type = field.getType();
        if (isStatic) {
            Object base = UNSAFE.staticFieldBase(field);
            long offset = UNSAFE.staticFieldOffset(field);
            if (!type.isPrimitive()) return new StaticObjectAccessor(base, offset);
            if (type == int.class) return new StaticIntAccessor(base, offset);
            if (type == long.class) return new StaticLongAccessor(base, offset);
            if (type == boolean.class) return new StaticBooleanAccessor(base, offset);
            if (type == byte.class) return new StaticByteAccessor(base, offset);
            if (type == short.class) return new StaticShortAccessor(base, offset);
            if (type == char.class) return new StaticCharAccessor(base, offset);
            if (type == float.class) return new StaticFloatAccessor(base, offset);
            if (type == double.class) return new StaticDoubleAccessor(base, offset);
        } else {
            long offset = UNSAFE.objectFieldOffset(field);
            if (!type.isPrimitive()) return new VirtualObjectAccessor(offset);
            if (type == int.class) return new VirtualIntAccessor(offset);
            if (type == long.class) return new VirtualLongAccessor(offset);
            if (type == boolean.class) return new VirtualBooleanAccessor(offset);
            if (type == byte.class) return new VirtualByteAccessor(offset);
            if (type == short.class) return new VirtualShortAccessor(offset);
            if (type == char.class) return new VirtualCharAccessor(offset);
            if (type == float.class) return new VirtualFloatAccessor(offset);
            if (type == double.class) return new VirtualDoubleAccessor(offset);
        }
        throw new IllegalArgumentException("Type: " + type);
    }

    private abstract static class BaseAccessor implements UnsafeFieldAccessor {
        protected final long offset;

        BaseAccessor(long offset) {
            this.offset = offset;
        }

        @Override
        public final long offset() {
            return offset;
        }
    }

    private static class VirtualObjectAccessor extends BaseAccessor {
        VirtualObjectAccessor(long o) {
            super(o);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) UNSAFE.getObject(obj, offset);
        }

        @Override
        public void set(Object obj, Object val) {
            UNSAFE.putObject(obj, offset, val);
        }
    }

    private static class VirtualIntAccessor extends BaseAccessor {
        VirtualIntAccessor(long o) {
            super(o);
        }

        @Override
        public int getInt(Object obj) {
            return UNSAFE.getInt(obj, offset);
        }

        @Override
        public void setInt(Object obj, int v) {
            UNSAFE.putInt(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Integer.valueOf(getInt(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setInt(obj, ((Number) val).intValue());
        }
    }

    private static class VirtualLongAccessor extends BaseAccessor {
        VirtualLongAccessor(long o) {
            super(o);
        }

        @Override
        public long getLong(Object obj) {
            return UNSAFE.getLong(obj, offset);
        }

        @Override
        public void setLong(Object obj, long v) {
            UNSAFE.putLong(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Long.valueOf(getLong(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setLong(obj, ((Number) val).longValue());
        }
    }

    private static class VirtualBooleanAccessor extends BaseAccessor {
        VirtualBooleanAccessor(long o) {
            super(o);
        }

        @Override
        public boolean getBoolean(Object obj) {
            return UNSAFE.getBoolean(obj, offset);
        }

        @Override
        public void setBoolean(Object obj, boolean v) {
            UNSAFE.putBoolean(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Boolean.valueOf(getBoolean(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setBoolean(obj, (Boolean) val);
        }
    }

    private static class VirtualByteAccessor extends BaseAccessor {
        VirtualByteAccessor(long o) {
            super(o);
        }

        @Override
        public byte getByte(Object obj) {
            return UNSAFE.getByte(obj, offset);
        }

        @Override
        public void setByte(Object obj, byte v) {
            UNSAFE.putByte(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Byte.valueOf(getByte(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setByte(obj, ((Number) val).byteValue());
        }
    }

    private static class VirtualShortAccessor extends BaseAccessor {
        VirtualShortAccessor(long o) {
            super(o);
        }

        @Override
        public short getShort(Object obj) {
            return UNSAFE.getShort(obj, offset);
        }

        @Override
        public void setShort(Object obj, short v) {
            UNSAFE.putShort(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Short.valueOf(getShort(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setShort(obj, ((Number) val).shortValue());
        }
    }

    private static class VirtualCharAccessor extends BaseAccessor {
        VirtualCharAccessor(long o) {
            super(o);
        }

        @Override
        public char getChar(Object obj) {
            return UNSAFE.getChar(obj, offset);
        }

        @Override
        public void setChar(Object obj, char v) {
            UNSAFE.putChar(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Character.valueOf(getChar(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setChar(obj, (Character) val);
        }
    }

    private static class VirtualFloatAccessor extends BaseAccessor {
        VirtualFloatAccessor(long o) {
            super(o);
        }

        @Override
        public float getFloat(Object obj) {
            return UNSAFE.getFloat(obj, offset);
        }

        @Override
        public void setFloat(Object obj, float v) {
            UNSAFE.putFloat(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Float.valueOf(getFloat(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setFloat(obj, ((Number) val).floatValue());
        }
    }

    private static class VirtualDoubleAccessor extends BaseAccessor {
        VirtualDoubleAccessor(long o) {
            super(o);
        }

        @Override
        public double getDouble(Object obj) {
            return UNSAFE.getDouble(obj, offset);
        }

        @Override
        public void setDouble(Object obj, double v) {
            UNSAFE.putDouble(obj, offset, v);
        }

        @Override
        public <V> V get(Object obj) {
            return (V) Double.valueOf(getDouble(obj));
        }

        @Override
        public void set(Object obj, Object val) {
            setDouble(obj, ((Number) val).doubleValue());
        }
    }

    private abstract static class BaseStaticAccessor extends BaseAccessor {
        protected final Object base;

        BaseStaticAccessor(Object b, long o) {
            super(o);
            this.base = b;
        }
    }

    private static class StaticObjectAccessor extends BaseStaticAccessor {
        StaticObjectAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public <V> V get(Object i) {
            return (V) UNSAFE.getObject(base, offset);
        }

        @Override
        public void set(Object i, Object v) {
            UNSAFE.putObject(base, offset, v);
        }
    }

    private static class StaticIntAccessor extends BaseStaticAccessor {
        StaticIntAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public int getInt(Object i) {
            return UNSAFE.getInt(base, offset);
        }

        @Override
        public void setInt(Object i, int v) {
            UNSAFE.putInt(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Integer.valueOf(getInt(null));
        }

        @Override
        public void set(Object i, Object v) {
            setInt(null, ((Number) v).intValue());
        }
    }

    private static class StaticLongAccessor extends BaseStaticAccessor {
        StaticLongAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public long getLong(Object i) {
            return UNSAFE.getLong(base, offset);
        }

        @Override
        public void setLong(Object i, long v) {
            UNSAFE.putLong(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Long.valueOf(getLong(null));
        }

        @Override
        public void set(Object i, Object v) {
            setLong(null, ((Number) v).longValue());
        }
    }

    private static class StaticBooleanAccessor extends BaseStaticAccessor {
        StaticBooleanAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public boolean getBoolean(Object i) {
            return UNSAFE.getBoolean(base, offset);
        }

        @Override
        public void setBoolean(Object i, boolean v) {
            UNSAFE.putBoolean(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Boolean.valueOf(getBoolean(null));
        }

        @Override
        public void set(Object i, Object v) {
            setBoolean(null, (Boolean) v);
        }
    }

    private static class StaticByteAccessor extends BaseStaticAccessor {
        StaticByteAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public byte getByte(Object i) {
            return UNSAFE.getByte(base, offset);
        }

        @Override
        public void setByte(Object i, byte v) {
            UNSAFE.putByte(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Byte.valueOf(getByte(null));
        }

        @Override
        public void set(Object i, Object v) {
            setByte(null, ((Number) v).byteValue());
        }
    }

    private static class StaticShortAccessor extends BaseStaticAccessor {
        StaticShortAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public short getShort(Object i) {
            return UNSAFE.getShort(base, offset);
        }

        @Override
        public void setShort(Object i, short v) {
            UNSAFE.putShort(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Short.valueOf(getShort(null));
        }

        @Override
        public void set(Object i, Object v) {
            setShort(null, ((Number) v).shortValue());
        }
    }

    private static class StaticCharAccessor extends BaseStaticAccessor {
        StaticCharAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public char getChar(Object i) {
            return UNSAFE.getChar(base, offset);
        }

        @Override
        public void setChar(Object i, char v) {
            UNSAFE.putChar(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Character.valueOf(getChar(null));
        }

        @Override
        public void set(Object i, Object v) {
            setChar(null, (Character) v);
        }
    }

    private static class StaticFloatAccessor extends BaseStaticAccessor {
        StaticFloatAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public float getFloat(Object i) {
            return UNSAFE.getFloat(base, offset);
        }

        @Override
        public void setFloat(Object i, float v) {
            UNSAFE.putFloat(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Float.valueOf(getFloat(null));
        }

        @Override
        public void set(Object i, Object v) {
            setFloat(null, ((Number) v).floatValue());
        }
    }

    private static class StaticDoubleAccessor extends BaseStaticAccessor {
        StaticDoubleAccessor(Object b, long o) {
            super(b, o);
        }

        @Override
        public double getDouble(Object i) {
            return UNSAFE.getDouble(base, offset);
        }

        @Override
        public void setDouble(Object i, double v) {
            UNSAFE.putDouble(base, offset, v);
        }

        @Override
        public <V> V get(Object i) {
            return (V) Double.valueOf(getDouble(null));
        }

        @Override
        public void set(Object i, Object v) {
            setDouble(null, ((Number) v).doubleValue());
        }
    }
}
