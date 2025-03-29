package io.wdsj.hybridfix.asm.plugin_patcher.annotation;

import io.wdsj.hybridfix.asm.plugin_patcher.ServerSoftware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplyTo {
    ServerSoftware value();
}
