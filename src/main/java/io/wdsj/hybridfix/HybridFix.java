package io.wdsj.hybridfix;

import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = HybridFix.MOD_ID, name = HybridFix.MOD_NAME, version = HybridFix.VERSION, dependencies = HybridFix.DEPENDENCY, serverSideOnly = true, acceptableRemoteVersions = "*")
public class HybridFix {
    public static final String MOD_ID = Tags.MOD_ID;
    public static final String MOD_NAME = Tags.MOD_NAME;
    public static final String VERSION = Tags.VERSION;
    public static final String DEPENDENCY = "required-after:mixinbooter@[7.1,);required-after:configanytime;";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final boolean IS_HYBRID_ENV = Utils.hasBukkit();
    public static final boolean HAS_CLEANROOM = Utils.isClassLoaded("com.cleanroommc.common.CleanroomContainer");

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (!IS_HYBRID_ENV) {
            LOGGER.warn("HybridFix requires a Forge+Bukkit server environment to work properly, disabling.");
            return;
        }
        if (HAS_CLEANROOM) {
            LOGGER.warn("CatRoom detected, it has most of HybridFix's patches, you may not benefit from this mod.");
            return;
        }
        HybridFixServer.preInit();
    }


    @Mod.EventHandler
    public void onServerStartComplete(FMLServerStartedEvent event) {
        HybridFixServer.onStartComplete();
    }
}
