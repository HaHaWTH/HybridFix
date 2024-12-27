package io.wdsj.hybridfix.util;

public class Utils {

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
