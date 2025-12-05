package io.wdsj.hybridfix.mixin.plugin_patcher;

import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.plugin_patcher.IPluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginPatcherManager;
import io.wdsj.hybridfix.config.Settings;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

@SuppressWarnings("ModifyVariableMayBeArgsOnly")
@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin extends URLClassLoader {
    @Unique
    private List<IPluginPatcher> hybridFix$pluginPatcher;

    @Unique
    private static IMixinTransformer hybridFix$mixinTransformer;

    static {
        if (Settings.pluginPatcherSettings.enableMixin) hybridFix$setupMixinTransformer();
    }

    public PluginClassLoaderMixin(URL[] urls) {
        super(urls);
    }

    @Unique
    private static void hybridFix$setupMixinTransformer() {
        Object active = MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
        if (!(active instanceof IMixinTransformer)) {
            HybridFix.LOGGER.error("Failed to get mixin transformer");
            return;
        }
        hybridFix$mixinTransformer = (IMixinTransformer) active;
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"
            )
    )
    public void preparePatcher(JavaPluginLoader loader, ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file, CallbackInfo ci) {
        this.hybridFix$pluginPatcher = PluginPatcherManager.INSTANCE.getPluginPatchers(description.getName());
    }

    @Dynamic("hybrid")
    @ModifyVariable(
            method = "remappedFindClass",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            )
    )
    public byte[] patch(byte[] bytecode, @Local(argsOnly = true) String name) {
        byte[] transformedBytecode = bytecode;
        if (this.hybridFix$pluginPatcher != null) {
            for (IPluginPatcher patcher : this.hybridFix$pluginPatcher) {
                patcher.setPluginClassLoader(this);
                transformedBytecode = patcher.transform(name, transformedBytecode);
                patcher.setPluginClassLoader(null);
            }
        }
        if (Settings.pluginPatcherSettings.enableMixin) {
            ClassLoader prevLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this);
                transformedBytecode = hybridFix$mixinTransformer.transformClass(MixinEnvironment.getCurrentEnvironment(), name, transformedBytecode);
            } finally {
                Thread.currentThread().setContextClassLoader(prevLoader);
            }
        }
        return transformedBytecode;
    }
}