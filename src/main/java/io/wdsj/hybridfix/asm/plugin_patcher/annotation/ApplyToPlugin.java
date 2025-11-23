package io.wdsj.hybridfix.asm.plugin_patcher.annotation;

import java.lang.annotation.*;

/**
 * Specify whether to apply the patch to the specified plugin.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplyToPlugin {
    String value();
}
