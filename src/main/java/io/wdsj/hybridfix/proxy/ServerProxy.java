package io.wdsj.hybridfix.proxy;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.HybridFixServer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

import static io.wdsj.hybridfix.HybridFix.LOGGER;

@SuppressWarnings("unused")
public class ServerProxy extends CommonProxy {
    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        if (!HybridFix.IS_HYBRID_ENV) {
            LOGGER.warn("HybridFix needs a Forge+Bukkit server environment to function, most features won't work in vanilla Forge.");
            return;
        }
        HybridFixServer.onPreInit();
    }

    @Override
    public void onServerStartComplete(FMLServerStartedEvent event) {
        super.onServerStartComplete(event);
        if (!HybridFix.IS_HYBRID_ENV) {
            return;
        }
        HybridFixServer.onStartComplete();
    }

    @Override
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        super.onServerAboutToStart(event);
        if (!HybridFix.IS_HYBRID_ENV) {
            return;
        }
        HybridFixServer.onServerAboutToStart();
    }
}
