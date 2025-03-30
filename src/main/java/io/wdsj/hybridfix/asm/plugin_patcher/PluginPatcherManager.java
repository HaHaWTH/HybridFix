package io.wdsj.hybridfix.asm.plugin_patcher;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.IBytecodePatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyTo;
import io.wdsj.hybridfix.asm.plugin_patcher.impl.CitizensPatcher;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.Utils;

import java.util.HashMap;
import java.util.Map;

public enum PluginPatcherManager {
    INSTANCE;
    private final Map<String, IBytecodePatcher> pluginPatcher = new HashMap<>();

    PluginPatcherManager() {
        IBytecodePatcher.clearDebugDumpDirectory();
        if (Settings.pluginPatcherSettings.patchCitizens) registerPluginPatcher("Citizens", new CitizensPatcher());
    }

    public IBytecodePatcher getPluginPatcher(String pluginName) {
        return pluginPatcher.get(pluginName);
    }

    private boolean registerPluginPatcher(String pluginName, IBytecodePatcher patcher) {
        try {
            ApplyTo applyTo = patcher.getClass().getAnnotation(ApplyTo.class);
            if (applyTo == null) {
                HybridFix.LOGGER.error("Plugin Patcher {} is not annotated with @ApplyTo", patcher.getClass().getSimpleName());
                return false;
            }
            ServerSoftware expectedServerSoftware = applyTo.value();
            boolean flag;
            switch (expectedServerSoftware) {
                case MOHIST:
                    flag = Utils.isMohist;
                    break;
                case CATSERVER:
                    flag = Utils.isCatServer;
                    break;
                case ALL:
                    flag = true;
                    break;
                case OTHERS:
                    flag = !Utils.isMohist && !Utils.isCatServer;
                    break;
                default:
                    flag = false;
                    break;
            }
            if (flag && !pluginPatcher.containsKey(pluginName)) {
                pluginPatcher.put(pluginName, patcher);
                return true;
            }
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to register plugin patcher {}", patcher.getClass().getSimpleName(), e);
        }
        return false;
    }
}
