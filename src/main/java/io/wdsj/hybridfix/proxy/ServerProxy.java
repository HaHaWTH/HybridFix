package io.wdsj.hybridfix.proxy;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.HybridFixServer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

import static io.wdsj.hybridfix.HybridFix.LOGGER;

@SuppressWarnings("unused")
public class ServerProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        if (!HybridFix.IS_HYBRID_ENV) {
            LOGGER.warn("HybridFix requires a Forge+Bukkit server environment to work properly, disabling.");
            return;
        }
        super.preInit(event);
        HybridFixServer.preInit();
    }

    @Override
    public void onServerStartComplete(FMLServerStartedEvent event) {
        if (!HybridFix.IS_HYBRID_ENV) {
            return;
        }
        super.onServerStartComplete(event);
        HybridFixServer.onStartComplete();
    }
}
