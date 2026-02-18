package io.wdsj.hybridfix.mixin.api.library_loader;

import io.wdsj.hybridfix.duck.api.library_loader.IJavaPluginLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.LibraryLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin {
    @Unique
    private ClassLoader libraryLoader;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;",
                    ordinal = 0
            )
    )
    private void init(JavaPluginLoader loader, ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file, CallbackInfo ci) {
        LibraryLoader lib = ((IJavaPluginLoader) (Object) loader).getLibraryLoader();
        libraryLoader = lib != null ? lib.createLoader(description) : null;
    }

    @Inject(
            method = "findClass(Ljava/lang/String;Z)Ljava/lang/Class;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void findClass(String name, boolean checkGlobal, CallbackInfoReturnable<Class<?>> cir) {
        if (libraryLoader != null) {
            try {
                Class<?> clazz = libraryLoader.loadClass(name);
                cir.setReturnValue(clazz);
            } catch (ClassNotFoundException ignored) {
            }
        }
    }
}
