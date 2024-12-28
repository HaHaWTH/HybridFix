package io.wdsj.hybridfix.util;

public class Utils {
    public static final String OBC_PACKAGE = "org.bukkit.craftbukkit.v1_12_R1";
    public static final boolean isMohist = isClassLoaded("com.mohistmc.MohistMC");

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
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
        return isClassLoaded("org.bukkit.Bukkit");
    }
}
