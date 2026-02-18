package io.wdsj.hybridfix.mixin.api.library_loader;

import io.wdsj.hybridfix.duck.api.library_loader.IJavaPluginLoader;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.LibraryLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = JavaPluginLoader.class, remap = false)
public abstract class JavaPluginLoaderMixin implements IJavaPluginLoader {
    @Shadow @Final Server server;
    @Unique
    private LibraryLoader libraryLoader;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void init(Server instance, CallbackInfo ci) {
        LibraryLoader libraryLoader = null;
        try {
            libraryLoader = new LibraryLoader(server.getLogger());
        } catch (NoClassDefFoundError ex) {
            // Provided depends were not added back
            server.getLogger().warning("Could not initialize LibraryLoader (missing dependencies?)");
            ex.printStackTrace();
        }
        this.libraryLoader = libraryLoader;
    }

    @Override
    public LibraryLoader getLibraryLoader() {
        return this.libraryLoader;
    }
}
