package io.wdsj.hybridfix.api.annotation;

import java.lang.annotation.*;

/**
 * Indicates the target class is a mod event that can be listened by Bukkit/Forge.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ModEvent {
    String value() default "forge";
}
