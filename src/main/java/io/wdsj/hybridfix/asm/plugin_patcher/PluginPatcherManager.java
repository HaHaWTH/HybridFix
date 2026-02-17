package io.wdsj.hybridfix.asm.plugin_patcher;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

import java.util.*;

public enum PluginPatcherManager {
    INSTANCE;
    private final Map<String, List<AbstractPluginPatcher>> pluginPatchers = new LinkedHashMap<>();

    PluginPatcherManager() {
        String patcherPackage = PluginPatcherManager.class.getPackage().getName() + ".impl";
        Class<?>[] classes = Utils.getClasses(patcherPackage).toArray(new Class[0]);
        ObjectArrays.quickSort(classes, Comparator.comparing(Class::getSimpleName));
        for (Class<?> clazz : classes) {
            try {
                if (AbstractPluginPatcher.class.isAssignableFrom(clazz)) {
                    boolean result = registerPluginPatcher((AbstractPluginPatcher) clazz.newInstance());
                    if (result) {
                        HybridFix.LOGGER.info("Registered plugin patcher {}", clazz.getSimpleName());
                    }
                }
            } catch (Exception e) {
                HybridFix.LOGGER.error("Failed to instantiate plugin patcher {}", clazz.getName(), e);
            }
        }
    }

    public List<AbstractPluginPatcher> getPluginPatchers(String pluginName) {
        return pluginPatchers.get(pluginName);
    }

    private boolean registerPluginPatcher(AbstractPluginPatcher patcher) {
        if (!Settings.pluginPatcherSettings.enable) {
            return false;
        }
        try {
            ApplyToPlugin applyToPlugin = patcher.getClass().getAnnotation(ApplyToPlugin.class);
            ApplyToPlugin.Configurable configurable = patcher.getClass().getAnnotation(ApplyToPlugin.Configurable.class);
            boolean applyToPluginExists = applyToPlugin != null;
            boolean configurableExists = configurable != null;
            if (!applyToPluginExists && !configurableExists) {
                HybridFix.LOGGER.error("Plugin patcher {} is not annotated with @ApplyToPlugin", patcher.getClass().getName());
                return false;
            }
            if (applyToPluginExists && configurableExists) {
                HybridFix.LOGGER.error("Found multiple annotations in plugin patcher {}, skipping.", patcher.getClass().getName());
            }
            if (applyToPluginExists) {
                return registerApplyTo(patcher, applyToPlugin);
            }
            return registerConfigurable(patcher, configurable);
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to register plugin patcher {}", patcher.getClass().getSimpleName(), e);
        }
        return false;
    }

    private boolean registerApplyTo(AbstractPluginPatcher patcher, ApplyToPlugin applyToPlugin) {
        String[] pluginNames = applyToPlugin.value();
        boolean flag = patcher.isEnabled();
        if (flag) {
            for (String pluginName : pluginNames) {
                register0(patcher, pluginName);
            }
            return true;
        }
        return false;
    }

    private boolean registerConfigurable(AbstractPluginPatcher patcher, ApplyToPlugin.Configurable ignored) {
        boolean flag = patcher.isEnabled();
        if (!(patcher instanceof ConfigurablePluginPatcher)) {
            HybridFix.LOGGER.error("Plugin patcher {} is not extending ConfigurablePluginPatcher, skipping.", patcher.getClass().getName());
            return false;
        }
        if (flag) {
            ConfigurablePluginPatcher configurablePatcher = (ConfigurablePluginPatcher) patcher;
            for (String pluginName : configurablePatcher.getTargetPlugins()) {
                register0(patcher, pluginName);
            }
            return true;
        }
        return false;
    }

    private void register0(AbstractPluginPatcher patcher, String pluginName) {
        pluginPatchers.compute(pluginName, (k, currentList) -> {
            if (currentList == null) {
                currentList = new ArrayList<>();
            }
            currentList.add(patcher);
            return currentList;
        });
    }
}