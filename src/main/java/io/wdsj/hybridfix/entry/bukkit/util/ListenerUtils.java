package io.wdsj.hybridfix.entry.bukkit.util;

import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.spigotmc.SneakyThrow;

import java.io.InputStream;
import java.lang.reflect.Method;

public class ListenerUtils {
    private ListenerUtils() {
    }

    /**
     * Hack to register a listener to target plugin ClassLoader that bypasses the isolation.
     * @param clazz listener class
     * @param pluginName target plugin
     */
    public static void registerListenerToTargetPlugin(Class<? extends Listener> clazz, String pluginName) {
        try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(
                clazz.getName().replace('.', '/') + ".class")) {
            assert inputStream != null;
            byte[] classBytes = new byte[inputStream.available()];
            inputStream.read(classBytes);
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
}
