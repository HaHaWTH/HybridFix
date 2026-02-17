package io.wdsj.hybridfix.asm.mod_patcher.annotation;

import io.wdsj.hybridfix.asm.plugin_patcher.ConfigurablePluginPatcher;

import java.lang.annotation.*;

/**
 * Specify mods this patch will apply to.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplyToMod {
    /**
     * Specify mod class names to apply to.
     * <p>
     * Example: {@code {"com.othermod.AClass", "net.anothermod.ABC"}}
     */
    String[] value();

    /**
     * Mark this patcher as configurable, must extend the {@link ConfigurablePluginPatcher} class.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Configurable {
    }
}