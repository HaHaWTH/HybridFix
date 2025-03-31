package io.wdsj.hybridfix.duck.bukkit.plugin;

public interface IPluginClassDefiner {
    Class<?> defineClassExposed(String name, byte[] bytes);
}
