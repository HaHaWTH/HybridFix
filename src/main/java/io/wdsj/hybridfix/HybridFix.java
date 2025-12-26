package io.wdsj.hybridfix;

import io.wdsj.hybridfix.proxy.CommonProxy;
import io.wdsj.hybridfix.util.Utils;
import net.lenni0451.reflect.JavaBypass;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = HybridFix.MOD_ID, name = HybridFix.MOD_NAME, version = HybridFix.VERSION, dependencies = HybridFix.DEPENDENCY, acceptableRemoteVersions = "*")
public class HybridFix {
    public static final String MOD_ID = Tags.MOD_ID;
    public static final String MOD_NAME = Tags.MOD_NAME;
    public static final String VERSION = Tags.VERSION;
    public static final String VERSION_CHANNEL = Tags.VERSION_CHANNEL;
    public static final String DEPENDENCY = "required-after:mixinbooter@[10.1,);required-after:configanytime;";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final boolean IS_HYBRID_ENV = Utils.isClassExists("org.bukkit.Bukkit");
    public static final boolean IS_CLEANROOM = Utils.isClassExists("com.cleanroommc.common.CleanroomContainer");
    static {
        try {
            JavaBypass.clearReflectionFilter();
        } catch (Throwable ignored) {
        }
    }
    @SidedProxy(
            clientSide = "io.wdsj.hybridfix.proxy.ClientProxy",
            serverSide = "io.wdsj.hybridfix.proxy.ServerProxy",
            modId = MOD_ID
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }


    @Mod.EventHandler
    public void onServerStartComplete(FMLServerStartedEvent event) {
        proxy.onServerStartComplete(event);
    }

    @Mod.EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.onServerAboutToStart(event);
    }

    // for mods
    @SuppressWarnings("unused")
    public static SupportStatus getSupportStatus() {
        if (!HybridFixPlugin.isClient) {
            if (IS_HYBRID_ENV) {
                return SupportStatus.FULL;
            } else {
                return SupportStatus.LIMITED;
            }
        }
        return SupportStatus.CLIENT;
    }

    public enum SupportStatus {
        FULL,
        CLIENT,
        LIMITED
    }
}
