package io.wdsj.hybridfix.util.reflection;

import io.wdsj.hybridfix.HybridFix;

import java.lang.reflect.Field;

public class BaseReflectionUtils {
    private BaseReflectionUtils() {
    }

    public static Object getObjectFieldValue(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while getting object value of field {}", field.getName(), e);
            return null;
        }
    }

    public static void setObjectFieldValue(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting object value of field {}", field.getName(), e);
        }
    }

    public static void setBooleanFieldValue(Field field, Object instance, boolean value) {
        try {
            field.setBoolean(instance, value);
        } catch (Exception e) {
            HybridFix.LOGGER.warn("Error occurred while setting boolean value of field {}", field.getName(), e);
        }
    }
}
