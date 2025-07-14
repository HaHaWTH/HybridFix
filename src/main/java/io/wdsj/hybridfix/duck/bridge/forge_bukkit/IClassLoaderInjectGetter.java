package io.wdsj.hybridfix.duck.bridge.forge_bukkit;

public interface IClassLoaderInjectGetter {
    default boolean isInjected() {
        return false;
    }
}
