package io.wdsj.hybridfix.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {
    public static final boolean isMohist = isClassExists("com.mohistmc.MohistMC");
    private static final ExecutorService commonWorker = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("HybridFix common worker - %d")
                    .setPriority(Thread.NORM_PRIORITY - 2)
                    .build()
    );

    public static ExecutorService commonWorker() {
        return commonWorker;
    }

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String classHierarchyToString(Class<?> startClass) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class Hierarchy:\n");

        Class<?> currentClass = startClass;
        int level = 0;

        while (currentClass != null) {
            for (int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append("-> ").append(currentClass.getName());

            if (level == 0) {
                Class<?>[] interfaces = currentClass.getInterfaces();
                if (interfaces.length > 0) {
                    sb.append(" (Implements: ");
                    for (int i = 0; i < interfaces.length; i++) {
                        sb.append(interfaces[i].getName());
                        if (i < interfaces.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append(")");
                }
            }

            sb.append("\n");

            currentClass = currentClass.getSuperclass();
            level++;
        }

        return sb.toString();
    }

    public static boolean isClassExists(String className) {
        String classPath = className.replace('.', '/') + ".class";
        try {
            return Thread.currentThread().getContextClassLoader().getResource(classPath) != null;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean isAnyClassLoaded(String... classNames) {
        for (String className : classNames) {
            if (isClassLoaded(className)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static IBlockState getBlockStateIfLoaded(World world, BlockPos pos) {
        Chunk chunk = world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk != null ? chunk.getBlockState(pos) : null;
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
