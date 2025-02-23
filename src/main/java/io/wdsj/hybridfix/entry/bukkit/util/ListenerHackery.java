package io.wdsj.hybridfix.entry.bukkit.util;

import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.spigotmc.SneakyThrow;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

public class ListenerHackery {
    private ListenerHackery() {
    }

    /*
    private static final boolean disableModuleSystemHack = Boolean.getBoolean("hybridfix.disableModuleSystemHack");
    static {
        if (getJavaMajorVersion() >= 17 && !disableModuleSystemHack) {
            HybridFix.LOGGER.info("Java version is 17+, attempting to hack into module system.");
            hackModuleSystem();
        }
    }
     */
    /**
     * Hack to register a listener to target plugin ClassLoader that bypasses the isolation.
     * @param clazz listener class
     * @param pluginName target plugin
     */
    public static void registerListenerToTargetPlugin(Class<? extends Listener> clazz, String pluginName) {
        try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(
                clazz.getName().replace('.', '/') + ".class")) {
            assert inputStream != null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] temp = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(temp, 0, temp.length)) != -1) {
                buffer.write(temp, 0, bytesRead);
            }
            byte[] classBytes = buffer.toByteArray();
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            assert plugin != null;
            // Inherit flow: PluginClassLoader -> URLClassLoader -> SecureClassLoader -> ClassLoader
            Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            Class<?> newClazz = (Class<?>) method.invoke(plugin.getClass().getClassLoader(), clazz.getName(), classBytes, 0, classBytes.length);
            Listener listener = (Listener) newClazz.newInstance();
            Bukkit.getPluginManager().registerEvents(listener, HybridFixInternalPlugin.getInstance());
        } catch (Throwable e) {
            SneakyThrow.sneaky(e);
        }
    }

    /*
    private static void hackModuleSystem() {
        try {
            Class<?> UnsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = UnsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Method getModule = Class.class.getMethod("getModule");
            Object objectModule = getModule.invoke(Object.class);

            long addr = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
            unsafe.getAndSetObject(ListenerHackery.class, addr, objectModule);
        } catch (Throwable e) {
            HybridFix.LOGGER.warn("Failed to hack into module system, you may need to manually add --add-opens=java.base/java.lang=ALL-UNNAMED to your startup flags to make plugin hooks work properly.", e);
        }
    }

    private static int getJavaMajorVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return version.charAt(2) - '0';
        }
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf("-"));
        }

        int dotIndex = version.indexOf(".");
        return Integer.parseInt(dotIndex == -1 ? version : version.substring(0, dotIndex));
    }
     */
}
