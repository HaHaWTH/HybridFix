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
    public static boolean passExplosionEventToBukkit = !Utils.isMohist;

    @Config.Comment("Remove entity damage & velocity on explosion being cancelled.")
    @Config.RequiresMcRestart
    public static boolean removeEntityDamageAndVelocityOnCancel = true;

    @Config.Comment("Override Mohist's crappy explosion handling with HybridFix's.")
    @Config.RequiresMcRestart
    public static boolean overrideMohistExplosionHandling = Utils.isMohist;

    @Config.Comment("Bridge Forge permissions to Bukkit.")
    @Config.RequiresMcRestart
    public static boolean bridgeForgePermissionsToBukkit = !Utils.isMohist;

    @Config.Comment("Skip firing event if no listeners registered.")
    @Config.RequiresMcRestart
    public static boolean skipEventIfNoListeners = true;

    @Config.Comment("Disable Spigot's built-in Timings to save performance.(Only support Timings v1)")
    @Config.RequiresMcRestart
    public static boolean disableTimings = false;

    @Config.Comment("Register HybridFix commands.")
    @Config.RequiresMcRestart
    public static boolean registerHybridFixCommands = true;

    static {
        ConfigAnytime.register(Settings.class);
    }
}
