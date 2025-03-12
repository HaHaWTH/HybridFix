package io.wdsj.hybridfix.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {
    public static final boolean isMohist = isClassExists("com.mohistmc.MohistMC");
    private static final boolean hasBukkit = isClassExists("org.bukkit.Bukkit");
    private static final ExecutorService ioWorker = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("HybridFix I/O worker-%d")
                    .setPriority(Thread.NORM_PRIORITY - 2)
                    .build()
    );

    public static ExecutorService ioWorker() {
        return ioWorker;
    }

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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

    public static boolean hasBukkit() {
        return hasBukkit;
    }
}
