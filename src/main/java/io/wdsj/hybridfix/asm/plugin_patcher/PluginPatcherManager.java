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
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public enum PluginPatcherManager {
    INSTANCE;
    private final Map<String, List<IPluginPatcher>> pluginPatcher = new ConcurrentHashMap<>();

    PluginPatcherManager() {
        IBytecodePatcher.clearDebugDumpDirectory();
        String PLUGIN_PATCHER_PACKAGE = PluginPatcherManager.class.getPackage().getName() + ".impl";
        Class<?>[] classes = getClasses(PLUGIN_PATCHER_PACKAGE).toArray(new Class[0]);
        ObjectArrays.quickSort(classes, Comparator.comparing(Class::getSimpleName));
        for (Class<?> clazz : classes) {
            try {
                if (IPluginPatcher.class.isAssignableFrom(clazz)) {
                    boolean result = registerPluginPatcher((IPluginPatcher) clazz.newInstance());
                    if (result) {
                        HybridFix.LOGGER.info("Registered plugin patcher {}", clazz.getSimpleName());
                    }
                }
            } catch (Exception e) {
                HybridFix.LOGGER.error("Failed to instantiate plugin patcher {}", clazz.getName(), e);
            }
        }
    }

    public List<IPluginPatcher> getPluginPatchers(String pluginName) {
        return pluginPatcher.get(pluginName);
    }

    private boolean registerPluginPatcher(IPluginPatcher patcher) {
        if (!Settings.pluginPatcherSettings.enable) {
            return false;
        }
        try {
            ApplyToPlugin applyToPlugin = patcher.getClass().getAnnotation(ApplyToPlugin.class);
            if (applyToPlugin == null) {
                HybridFix.LOGGER.error("Plugin patcher {} is not annotated with @ApplyToPlugin", patcher.getClass().getName());
                return false;
            }
            String pluginName = applyToPlugin.value();
            boolean flag = patcher.isEnabled();
            if (flag) {
                pluginPatcher.computeIfPresent(pluginName, (k, v) -> {
                    v.add(patcher);
                    return v;
                });
                pluginPatcher.computeIfAbsent(pluginName, k -> {
                    final List<IPluginPatcher> list = new ArrayList<>();
                    list.add(patcher);
                    return list;
                });
                return true;
            }
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to register plugin patcher {}", patcher.getClass().getSimpleName(), e);
        }
        return false;
    }


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

        File[] dirfiles = dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".class"));
        if (dirfiles != null) {
            for (File file : dirfiles) {
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