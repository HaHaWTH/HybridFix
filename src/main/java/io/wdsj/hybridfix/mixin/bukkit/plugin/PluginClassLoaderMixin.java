package io.wdsj.hybridfix.mixin.bukkit.plugin;

import io.wdsj.hybridfix.duck.bukkit.plugin.IPluginClassDefiner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.net.URL;
import java.net.URLClassLoader;

@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin extends URLClassLoader implements IPluginClassDefiner {
    public PluginClassLoaderMixin(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public Class<?> defineClassExposed(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
