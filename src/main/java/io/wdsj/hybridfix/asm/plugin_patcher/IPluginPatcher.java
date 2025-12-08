package io.wdsj.hybridfix.asm.plugin_patcher;

import io.wdsj.hybridfix.asm.IBytecodePatcher;
import org.bukkit.plugin.PluginDescriptionFile;

public interface IPluginPatcher extends IBytecodePatcher {
    default ClassLoader getPluginClassLoader() {
        return null;
    }

    default void setPluginClassLoader(ClassLoader classLoader) {
    }

    default void setPluginDescriptionFile(PluginDescriptionFile descriptionFile) {
    }

    default PluginDescriptionFile getPluginDescriptionFile() {
        return null;
    }

    static boolean isCommonPackage(String packageName) {
        return packageName.contains("fastutil") || packageName.contains("org.apache") || packageName.contains("javax");
    }
}
