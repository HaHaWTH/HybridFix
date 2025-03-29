package io.wdsj.hybridfix.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {
    public static final boolean isMohist = isClassExists("com.mohistmc.MohistMC");
    public static final boolean isCatServer = isClassExists("catserver.server.CatServer");
    private static final boolean hasBukkit = isClassExists("org.bukkit.Bukkit");
    private static final ExecutorService commonWorker = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("HybridFix common worker-%d")
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
