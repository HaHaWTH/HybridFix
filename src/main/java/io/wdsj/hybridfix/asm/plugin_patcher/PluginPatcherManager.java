package io.wdsj.hybridfix.asm.plugin_patcher;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.IBytecodePatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public enum PluginPatcherManager {
    INSTANCE;
    private final Map<String, List<AbstractPluginPatcher>> pluginPatchers = new LinkedHashMap<>();

    PluginPatcherManager() {
        IBytecodePatcher.clearDebugDumpDirectory();
        String patcherPackage = PluginPatcherManager.class.getPackage().getName() + ".impl";
        Class<?>[] classes = getClasses(patcherPackage).toArray(new Class[0]);
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

    // Adapted from Winds-Studio/Leaf, licensed under MIT
    public static @NotNull Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageDirName = pack.replace('.', '/');
        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());
                    findClassesInPackageByFile(pack, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        findClassesInPackageByJar(pack, entries, packageDirName, classes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return classes;
    }

    private static void findClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirFiles = dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".class"));
        if (dirFiles != null) {
            for (File file : dirFiles) {
                if (file.isDirectory()) {
                    findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, Set<Class<?>> classes) {
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }

            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');

                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }

                if (name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}