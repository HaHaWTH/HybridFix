package io.wdsj.hybridfix.duck.bukkit.plugin;

public interface IPluginClassDefiner {
    /**
     * Expose {@link ClassLoader#defineClass(String, byte[], int, int)} to external code.
     * @param name Binary name of the class
     * @param bytes Class bytes
     * @return The Class object that was created from the specified class data.
     */
    Class<?> defineClassExposed(String name, byte[] bytes);
}
