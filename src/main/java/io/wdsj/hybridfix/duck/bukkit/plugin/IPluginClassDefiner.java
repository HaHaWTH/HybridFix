package io.wdsj.hybridfix.duck.bukkit.plugin;

import io.wdsj.hybridfix.HybridFix;

public interface IPluginClassDefiner {
    /**
     * Expose {@link ClassLoader#defineClass(String, byte[], int, int)} to external code.
     * @param name Binary name of the class
     * @param bytes Class bytes
     * @return The Class object that was created from the specified class data.
     */
    default Class<?> defineClassExposed(String name, byte[] bytes) {
        HybridFix.LOGGER.error("This method is not implemented by PluginClassLoader, did you forget to enable it in HybridFix config?");
        throw new IllegalStateException("Not implemented.");
    }
}
