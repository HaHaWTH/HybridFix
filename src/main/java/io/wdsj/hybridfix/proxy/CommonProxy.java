package io.wdsj.hybridfix.proxy;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.raknetify.RaknetifyBootstrap112;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

public class CommonProxy {
    public void onPreInit(FMLPreInitializationEvent event) {
        HybridFix.LOGGER.info("HybridFix support status: {}", HybridFix.getSupportStatus().toString());
        if (Settings.raknetify.enable) {
            RaknetifyBootstrap112.initialize();
        }
    }

    public void onServerStartComplete(FMLServerStartedEvent event) {
    }

    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
    }
}
