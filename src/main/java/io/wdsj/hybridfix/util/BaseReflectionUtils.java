package io.wdsj.hybridfix.util;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.HybridFixServer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BaseReflectionUtils {
    private BaseReflectionUtils() {
    }
    public static MethodHandle getMethodHandle_Constructor(Class<?> clazz, Class<?>... params) {
        try {
            Constructor<?> c = clazz.getConstructor(params);
            c.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(c);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while getting method handle of constructor {}", clazz.getName());
            HybridFixServer.createServerDump(e);
            return null;
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while getting field {}", fieldName);
            HybridFixServer.createServerDump(e);
            return null;
        }
    }

    public static Object getFieldValue(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while getting value of field {}", field.getName(), e);
            return null;
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting value of field {}", field.getName(), e);
        }
    }

    public static void setBooleanField(Field field, Object instance, boolean value) {
        try {
            field.setBoolean(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting boolean value of field {}", field.getName(), e);
        }
    }

    public static Method getMethod(String className, String methodName, Class<?>... paramTypes) {
        try {
            Class<?> clazz = Class.forName(className);
            Method m = clazz.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?> getConstructor(String className, Class<?>... paramTypes) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> c = clazz.getDeclaredConstructor(paramTypes);
            c.setAccessible(true);
            return c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(String className, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
