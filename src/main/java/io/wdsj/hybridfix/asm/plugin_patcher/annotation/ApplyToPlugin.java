package io.wdsj.hybridfix.asm.plugin_patcher.annotation;

import io.wdsj.hybridfix.asm.plugin_patcher.ConfigurablePluginPatcher;

import java.lang.annotation.*;

/**
 * Specify plugins this patch will apply to.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplyToPlugin {
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
