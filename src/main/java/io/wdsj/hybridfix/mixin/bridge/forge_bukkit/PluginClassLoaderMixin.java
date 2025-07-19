package io.wdsj.hybridfix.mixin.bridge.forge_bukkit;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.duck.bridge.forge_bukkit.IClassLoaderInjectGetter;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin extends URLClassLoader implements IClassLoaderInjectGetter {
    public PluginClassLoaderMixin(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    @Unique
    private static final Method hybridFix$addChild;
    @Unique
    private boolean hybridFix$isInjected;

    static {
        Method add;
        try {
            // noinspection JavaReflectionMemberAccess
            add = LaunchClassLoader.class.getMethod("addChild", ClassLoader.class);
        } catch (Throwable e) {
            add = null;
            HybridFix.LOGGER.error("Failed to find method LaunchClassLoader#addChild", e);
        }
        hybridFix$addChild = add;
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void onInit(JavaPluginLoader loader, ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file, CallbackInfo ci) {
        if (!(parent instanceof LaunchClassLoader) || hybridFix$addChild == null) {
            return;
        }
        try {
            hybridFix$addChild.invoke(parent, this);
            hybridFix$isInjected = true;
            HybridFix.LOGGER.debug("Injected {} classloader into LaunchClassLoader", description.getName());
        } catch (Throwable e) {
            HybridFix.LOGGER.error("Failed to add plugin class loader to LaunchClassLoader", e);
        }
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public boolean isInjected() {
        return hybridFix$isInjected;
    }
}
