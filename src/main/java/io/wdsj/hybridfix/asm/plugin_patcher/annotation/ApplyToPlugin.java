package io.wdsj.hybridfix.asm.plugin_patcher.annotation;

import io.wdsj.hybridfix.asm.plugin_patcher.IConfigurablePluginPatcher;

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
     * Mark this patcher as configurable, must implement the {@link IConfigurablePluginPatcher} interface.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Configurable {
    }
}
