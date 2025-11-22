package io.wdsj.hybridfix.mixin.plugin_patcher;

import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.asm.IBytecodePatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginPatcherManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.List;

@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin {
    @Unique
    private List<IBytecodePatcher> hybridFix$pluginPatcher;

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
            ),
            require = 0
    )
    public byte[] patch(byte[] bytecode, @Local(argsOnly = true) String name) {
        byte[] transformedBytecode = bytecode;
        if (this.hybridFix$pluginPatcher != null) {
            for (IBytecodePatcher patcher : this.hybridFix$pluginPatcher) {
                patcher.setPluginClassLoader((ClassLoader) (Object) this);
                transformedBytecode = patcher.transform(name, bytecode);
            }
        }
        return transformedBytecode;
    }
}