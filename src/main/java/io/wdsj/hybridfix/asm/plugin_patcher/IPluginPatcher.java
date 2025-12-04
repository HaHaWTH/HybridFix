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

    /**
     * Need to be implemented if the patcher is annotated with {@link io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin.Configurable}.
     */
    default String[] getTargetPlugins() {
        return new String[0];
    }
}
