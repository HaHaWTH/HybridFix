package io.wdsj.hybridfix.util.reflection;

public interface UnsafeFieldAccessor {
    <V> V get(Object instance);
    void set(Object instance, Object value);
    long offset();

    default int getInt(Object instance) {
        throw new UnsupportedOperationException("Not an int field");
    }

    default void setInt(Object instance, int value) {
        throw new UnsupportedOperationException("Not an int field");
    }

    default long getLong(Object instance) {
        throw new UnsupportedOperationException("Not a long field");
    }

    default void setLong(Object instance, long value) {
        throw new UnsupportedOperationException("Not a long field");
    }

    default boolean getBoolean(Object instance) {
        throw new UnsupportedOperationException("Not a boolean field");
    }

    default void setBoolean(Object instance, boolean value) {
        throw new UnsupportedOperationException("Not a boolean field");
    }

    default byte getByte(Object instance) {
        throw new UnsupportedOperationException("Not a byte field");
    }

    default void setByte(Object instance, byte value) {
        throw new UnsupportedOperationException("Not a byte field");
    }

    default short getShort(Object instance) {
        throw new UnsupportedOperationException("Not a short field");
    }

    default void setShort(Object instance, short value) {
        throw new UnsupportedOperationException("Not a short field");
    }

    default char getChar(Object instance) {
        throw new UnsupportedOperationException("Not a char field");
    }

    default void setChar(Object instance, char value) {
        throw new UnsupportedOperationException("Not a char field");
    }

    default float getFloat(Object instance) {
        throw new UnsupportedOperationException("Not a float field");
    }

    default void setFloat(Object instance, float value) {
        throw new UnsupportedOperationException("Not a float field");
    }

    default double getDouble(Object instance) {
        throw new UnsupportedOperationException("Not a double field");
    }

    default void setDouble(Object instance, double value) {
        throw new UnsupportedOperationException("Not a double field");
    }
}
