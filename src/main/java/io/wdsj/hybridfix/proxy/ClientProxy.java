package io.wdsj.hybridfix.proxy;

import net.minecraftforge.fml.common.event.*;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void onServerStartComplete(FMLServerStartedEvent event) {
        super.onServerStartComplete(event);
    }
}
