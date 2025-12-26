package io.wdsj.hybridfix.proxy;

import io.wdsj.hybridfix.HybridFix;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HybridFix.LOGGER.info("HybridFix support status: {}", HybridFix.getSupportStatus().toString());
    }

    public void onServerStartComplete(FMLServerStartedEvent event) {
    }

    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
    }
}
