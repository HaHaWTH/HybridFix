package io.wdsj.hybridfix.mixin.fix.forge.server_language;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(value = FMLServerHandler.class, remap = false)
public abstract class FMLServerHandlerMixin {
    @Unique
    private static final String[] hybridFix$splitLang = Settings.preferredLanguage.split(":");
    static {
        if (hybridFix$splitLang.length > 1) {
            InputStream inputstream = HybridFix.class.getResourceAsStream("/server_assets/minecraft/lang/" + hybridFix$splitLang[0] + ".lang");
            try {
                if (inputstream != null) {
                    LanguageMap.inject(inputstream);
                }
                HybridFix.LOGGER.info("Injected {}.lang for Minecraft", hybridFix$splitLang[0]);
            } catch (Exception ignored) {
            } finally {
                IOUtils.closeQuietly(inputstream);
            }
        }
    }
    @Inject(
            method = "addModAsResource",
            at = @At(
                    value = "RETURN"
            )
    )
    private static void injectExtraLangFiles(ModContainer container, CallbackInfo ci) {
        if (hybridFix$splitLang.length < 2) return;
        String langFile = "assets/" + container.getModId().toLowerCase() + "/lang/" + hybridFix$splitLang[0] + ".lang";
        String langFile2 = "assets/" + container.getModId().toLowerCase() + "/lang/" + hybridFix$splitLang[1] + ".lang";
        File source = container.getSource();
        InputStream stream = null;
        ZipFile zip = null;
        try {
            if (source.isDirectory() && FMLLaunchHandler.isDeobfuscatedEnvironment()) {
                File f = new File(source.toURI().resolve(langFile).getPath());
                if (!f.exists())
                    f = new File(source.toURI().resolve(langFile2).getPath());
                if (!f.exists())
                    throw new FileNotFoundException(source.toURI().resolve(langFile).getPath());
                stream = new FileInputStream(f);
            } else if (source.exists()) //Fake sources.. Yay coremods -.-
            {
                zip = new ZipFile(source);
                ZipEntry entry = zip.getEntry(langFile);
                if (entry == null) entry = zip.getEntry(langFile2);
                if (entry == null) throw new FileNotFoundException(langFile);
                stream = zip.getInputStream(entry);
            }
            if (stream != null)
                LanguageMap.inject(stream);
        } catch (FileNotFoundException e) {
            FMLLog.log.debug("Missing {} translation for {}: {}", hybridFix$splitLang[0], container.getModId(), e.getMessage());
        } catch (IOException e) {
            // hush
        } catch (Exception e) {
            FMLLog.log.error(e);
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(zip);
        }
    }
}
