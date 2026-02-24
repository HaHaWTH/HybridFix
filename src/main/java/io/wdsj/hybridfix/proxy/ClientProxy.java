package io.wdsj.hybridfix.proxy;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.AbstractResidenceDataSender;
import io.wdsj.hybridfix.handler.voxelmap.VoxelMapResidenceStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        if (Settings.modPatchSettings.voxelMapResidenceSupport && Loader.isModLoaded("voxelmap")) {
            FMLEventChannel channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(AbstractResidenceDataSender.CHANNEL);
            channel.register(VoxelMapResidenceStorage.INSTANCE);
            MinecraftForge.EVENT_BUS.register(VoxelMapResidenceStorage.INSTANCE);
        }
    }

    @Override
    public void onServerStartComplete(FMLServerStartedEvent event) {
        super.onServerStartComplete(event);
    }

    @Override
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        super.onServerAboutToStart(event);
    }
}
