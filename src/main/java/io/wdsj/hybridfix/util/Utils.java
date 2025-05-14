package io.wdsj.hybridfix.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {
    public static final boolean isMohist = isClassExists("com.mohistmc.MohistMC");
    private static final boolean hasBukkit = isClassExists("org.bukkit.Bukkit");
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

    public static boolean hasBukkit() {
        return hasBukkit;
    }
}
