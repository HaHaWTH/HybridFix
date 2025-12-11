package io.wdsj.hybridfix.asm.plugin_patcher;

import io.wdsj.hybridfix.asm.IBytecodePatcher;
import org.bukkit.plugin.PluginDescriptionFile;

public abstract class AbstractPluginPatcher implements IBytecodePatcher {
    private ClassLoader classLoader;
    private PluginDescriptionFile pdf;
    public ClassLoader getPluginClassLoader() {
        return this.classLoader;
    }

    public void setPluginClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setPluginDescriptionFile(PluginDescriptionFile descriptionFile) {
        this.pdf = descriptionFile;
    }

    public PluginDescriptionFile getPluginDescriptionFile() {
        return this.pdf;
    }

    protected static boolean isCommonPackage(String packageName) {
        return packageName.contains("fastutil") || packageName.contains("org.apache") || packageName.contains("javax");
    }
}
