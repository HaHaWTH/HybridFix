package io.wdsj.hybridfix.mixin.plugin_patcher;

import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.api.forge.event.bukkit_mixin.BukkitMixinSetupEvent;
import io.wdsj.hybridfix.asm.plugin_patcher.AbstractPluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.PluginPatcherManager;
import io.wdsj.hybridfix.config.Settings;
import net.minecraftforge.common.MinecraftForge;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.extensibility.IMixinProcessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ModifyVariableMayBeArgsOnly")
@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin extends URLClassLoader {
    @Shadow @Final private PluginDescriptionFile description;
    @Unique private List<AbstractPluginPatcher> hybridFix$pluginPatchers;
    @Unique private static IMixinTransformer hybridFix$mixinTransformer;

    static {
        if (Settings.pluginPatcherSettings.enableMixin) hybridFix$setupMixins();
    }

    public PluginClassLoaderMixin(URL[] urls) {
        super(urls);
    }

    @Unique
    private static void hybridFix$setupMixins() {
        Object active = MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
        if (!(active instanceof IMixinTransformer)) {
            HybridFix.LOGGER.error("Failed to get mixin transformer");
            return;
        }
        BukkitMixinSetupEvent event = new BukkitMixinSetupEvent(new ArrayList<>());
        MinecraftForge.EVENT_BUS.post(event);
        for (String mixinConfig : event.getMixinConfigs()) {
            Mixins.addConfiguration(mixinConfig);
            HybridFix.LOGGER.info("Adding plugin mixin config: {}", mixinConfig);
        }
        try {
            Field delegatedTransformersField = MixinServiceLaunchWrapper.class.getDeclaredField("delegatedTransformers");
            delegatedTransformersField.setAccessible(true);
            delegatedTransformersField.set(MixinService.getService(), null);

            IMixinProcessor processor = Proxy.transformer.getProcessor();
            Method selectMethod = processor.getClass().getDeclaredMethod("select", MixinEnvironment.class);
            selectMethod.setAccessible(true);
            selectMethod.invoke(processor, MixinEnvironment.getCurrentEnvironment());
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to setup mixins", e);
        }
        hybridFix$mixinTransformer = (IMixinTransformer) active;
        HybridFix.LOGGER.info("Bukkit plugin mixin setup complete");
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"
            )
    )
    public void preparePatcher(JavaPluginLoader loader, ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file, CallbackInfo ci) {
        this.hybridFix$pluginPatchers = PluginPatcherManager.INSTANCE.getPluginPatchers(description.getName());
    }

    @Dynamic("hybrid")
    @ModifyVariable(
            method = "remappedFindClass",
            at = @At(
                    value = "STORE",
                    ordinal = 2
            )
    )
    public byte[] patchAfterRemap(byte[] bytecode, @Local(argsOnly = true) String name) {
        return hybridFix$patch0(bytecode, name);
    }

    @Unique
    private byte[] hybridFix$patch0(byte[] bytecode, String name) {
        byte[] transformedBytecode = bytecode;
        if (this.hybridFix$pluginPatchers != null) {
            for (AbstractPluginPatcher patcher : this.hybridFix$pluginPatchers) {
                patcher.setPluginClassLoader(this);
                patcher.setPluginDescriptionFile(description);
                transformedBytecode = patcher.transform(name, name, transformedBytecode); // Untransformed name is for mods
                patcher.setPluginClassLoader(null);
                patcher.setPluginDescriptionFile(null);
            }
        }
        if (Settings.pluginPatcherSettings.enableMixin && hybridFix$mixinTransformer != null) {
            ClassLoader prevLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this);
                transformedBytecode = hybridFix$mixinTransformer.transformClass(MixinEnvironment.getCurrentEnvironment(), name, transformedBytecode);
            } catch (Exception e) {
                HybridFix.LOGGER.error("Failed to apply plugin mixin", e);
            } finally {
                Thread.currentThread().setContextClassLoader(prevLoader);
            }
        }
        return transformedBytecode;
    }
}