package io.wdsj.hybridfix.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.common.config.Config;

@Config(modid = HybridFix.MOD_ID)
public class Settings {
    @Config.Comment("Fix Simple Difficulty(And other similar mods) thirst not getting reset on respawn.")
    @Config.RequiresMcRestart
    public static boolean fixCapabilityReset = true;

    @Config.Comment("Pass explosion event to Bukkit.")
    @Config.RequiresMcRestart
    public static boolean passExplosionEventToBukkit = !Utils.isClassLoaded("com.mohistmc.MohistMC"); // Default enable if not Mohist (Honestly Mohist's explosion handling is shit)

    static {
        ConfigAnytime.register(Settings.class);
    }
}
