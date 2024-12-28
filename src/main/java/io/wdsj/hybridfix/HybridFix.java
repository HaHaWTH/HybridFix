package io.wdsj.hybridfix;

import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = HybridFix.MOD_ID, name = HybridFix.MOD_NAME, version = HybridFix.VERSION, dependencies = HybridFix.DEPENDENCY, serverSideOnly = true, acceptableRemoteVersions = "*")
public class HybridFix {
    public static final String MOD_ID = "hybridfix";
    public static final String MOD_NAME = "HybridFix";
    public static final String VERSION = "0.0.1";
    public static final String DEPENDENCY = "required-after:mixinbooter@[8.8,);required-after:configanytime;";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final boolean IS_HYBRID_ENV = Utils.hasBukkit();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (!IS_HYBRID_ENV) {
            LOGGER.warn("HybridFix requires a Forge+Bukkit server environment to work properly, disabling.");
            return;
        }
        HybridFixServer.preInit();
    }
}
