package io.wdsj.hybridfix.util;

import io.wdsj.hybridfix.util.reflection.FluentReflect;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for getting the caller class.
 */
@SuppressWarnings({"unchecked", "unused"})
public class Caller {
    private Caller() {
    }

    private static final Method SUN_REFLECT_REFLECTION_getCallerClass = FluentReflect.fromClass("sun.reflect.Reflection")
            .name("getCallerClass")
            .accessible(true)
            .param(int.class)
            .findDeclaredMethod()
            .orElse(null);
    private static final Method STACK_WALKER_getInstance = FluentReflect.fromClass("java.lang.StackWalker")
            .name("getInstance")
            .accessible(true)
            .params(Set.class, int.class)
            .findDeclaredMethod()
            .orElse(null);
    private static final Object RETAIN_CLASS_REFERENCE = FluentReflect.fromClass("java.lang.StackWalker$Option")
            .as(Enum.class)
            .findType()
            .map(enumClass -> Enum.valueOf(enumClass, "RETAIN_CLASS_REFERENCE"))
            .orElse(null);
    private static final Class<?> STACK_WALKER_CLASS = FluentReflect.fromClass("java.lang.StackWalker")
            .findType()
            .orElse(null);
    private static final Class<?> STACK_FRAME_CLASS = FluentReflect.fromClass("java.lang.StackWalker$StackFrame")
            .findType()
            .orElse(null);
    private static final Method STACK_FRAME_getDeclaringClass = FluentReflect.fromClass(STACK_FRAME_CLASS)
            .name("getDeclaringClass")
            .accessible(true)
            .findDeclaredMethod()
            .orElse(null);
    private static final Method STACK_WALKER_walk = FluentReflect.fromClass(STACK_WALKER_CLASS)
            .name("walk")
            .accessible(true)
            .param(Function.class)
            .findDeclaredMethod()
            .orElse(null);

    private static final int OVERLOAD_METHOD_DEFAULT_SKIP_FRAMES = 1;
    public static Class<?> getCallerClass() {
        return getCallerClass(OVERLOAD_METHOD_DEFAULT_SKIP_FRAMES);
    }

    /**
     * Returns the class that called the method, preferring to {@link sun.reflect.Reflection} if available.
     *
     * @param skipFrames The number of frames to skip.
     * @return The class that called the method.
     */
    public static Class<?> getCallerClass(int skipFrames) {
        if (SUN_REFLECT_REFLECTION_getCallerClass != null) {
            try {
                return (Class<?>) SUN_REFLECT_REFLECTION_getCallerClass.invoke(null, skipFrames + 3);
            } catch (Exception th) {
                SneakyThrow.sneaky(th);
            }
        }
        if (STACK_WALKER_CLASS != null && STACK_FRAME_CLASS != null) {
            try {
                Set<Object> options = Collections.singleton(RETAIN_CLASS_REFERENCE);
                Object stackWalkerInstance = STACK_WALKER_getInstance.invoke(null, options, skipFrames + 2);

                Function<Stream<?>, Class<?>> walkFunction = stream -> {
                    try {
                        return stream
                                .map(frame -> {
                                    try {
                                        return (Class<?>) STACK_FRAME_getDeclaringClass.invoke(frame);
                                    } catch (Exception e) {
                                        SneakyThrow.sneaky(e);
                                        return null;
                                    }
                                })
                                .skip(skipFrames + 2)
                                .findFirst()
                                .orElse(null);
                    } catch (Exception e) {
                        SneakyThrow.sneaky(e);
                        return null;
                    }
                };

                return (Class<?>) STACK_WALKER_walk.invoke(stackWalkerInstance, walkFunction);
            } catch (Exception e) {
                SneakyThrow.sneaky(e);
                return null;
            }
        }
        throw new IllegalStateException("No supported methods found.");
    }
}