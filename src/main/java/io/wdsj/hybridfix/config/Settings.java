package io.wdsj.hybridfix.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.common.config.Config;

@Config(modid = HybridFix.MOD_ID)
public class Settings {
    @Config.Comment("Check for updates on startup.")
    @Config.RequiresMcRestart
    public static boolean checkForUpdates = true;

    @Config.Comment("Re-gather capabilities on respawn, will fix Simple Difficulty(And other similar mods) thirst not getting reset on respawn.\nAnd fix dupe bug in The Betweenlands.")
    @Config.RequiresMcRestart
    public static boolean fixCapabilityReset = false;

    @Config.Comment("Simulate vanilla respawn logic.")
    @Config.RequiresMcRestart
    public static boolean simulateVanillaRespawn = true;

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

    @Config.Comment("Enable HybridFix's CraftServer optimizations.")
    @Config.RequiresMcRestart
    public static boolean enableCraftServerOptimizations = true;

    @Config.Comment("Disable Spigot's built-in Timings to save performance.(Only support Timings v1)")
    @Config.RequiresMcRestart
    public static boolean disableTimings = false;

    @Config.Comment("Register HybridFix commands.")
    @Config.RequiresMcRestart
    public static boolean registerHybridFixCommands = true;

    @Config.Comment("Enable HybridFix's bStats metrics.")
    @Config.RequiresMcRestart
    public static boolean enableMetrics = true;

    @Config.Comment("FakePlayer event blacklist, plugins in this list will NOT receive events fired by fake players.")
    @Config.RequiresMcRestart
    public static String[] fakePlayerPluginBlacklist = new String[]{};

    @Config.Comment("Whether to invert fake player blacklist to whitelist")
    @Config.RequiresMcRestart
    public static boolean invertFakePlayerBlacklist = false;

    @Config.Comment("[EXPERIMENTAL] More Bukkit API implementation.")
    @Config.RequiresMcRestart
    public static boolean extraBukkitApi = false;

    @Config.Comment("SDK integration for RaytraceAntiXray.")
    @Config.RequiresMcRestart
    public static boolean rayTraceAntiXraySDK = false;

    @Config.Comment("Configuration for HybridFix mod patches.")
    @Config.RequiresMcRestart
    public static ModPatchSettings modPatchSettings = new ModPatchSettings();

    public static class ModPatchSettings {
        @Config.Comment("Patch saplings in twilight forest bypass Bukkit grief protections exploit.")
        @Config.RequiresMcRestart
        public boolean patchTwilightForestSapling = true;

        @Config.Comment("Patch entities in twilight forest (e.g. Naga) bypass Bukkit grief protections exploit.")
        @Config.RequiresMcRestart
        public boolean patchTwilightForestEntityEvent = true;

        @Config.Comment("Patch items in twilight forest (e.g. MagicBeans) bypass Bukkit grief protections exploit.")
        @Config.RequiresMcRestart
        public boolean patchTwilightForestItem = true;

        @Config.Comment("Patch taint in thaumcraft spread event.")
        @Config.RequiresMcRestart
        public boolean patchThaumcraftTaintSpread = true;

        @Config.Comment("Patch flux rift in thaumcraft.")
        @Config.RequiresMcRestart
        public boolean patchThaumcraftFlux = true;

        @Config.Comment("Patch Tinkers Construct tool damage,\nUseful for some traits that adds extra effects and disarming, etc.\nYou can use property -Dhybridfix.tconstruct.supersedeVanillaEvent=true to prevent calling original damage event.")
        @Config.RequiresMcRestart
        public boolean patchTconstructToolDamage = false;

        @Config.Comment("Patch Disarm enchantment in SME(1.0.0 and higher), prevent bypassing the bukkit protection.")
        @Config.RequiresMcRestart
        public boolean patchSoManyEnchantmentsDisarm = false;

        @Config.Comment("Patch lens of Botania can bypass bukkit grief protection.")
        @Config.RequiresMcRestart
        public boolean patchBotaniaLens = false;

        @Config.Comment("Patch Rannuncarpus can bypass bukkit grief protection.\nNOTE: This fix uses FakePlayer!")
        @Config.RequiresMcRestart
        public boolean patchBotaniaBlock = false;

        @Config.Comment("Patch miner machines of Industrial Craft 2 can mine blocks in protected areas.\nNOTE: This fix uses FakePlayer!")
        @Config.RequiresMcRestart
        public boolean patchIC2Machine = false;

        @Config.Comment("Patch explosions of Industrial Craft 2 can break blocks in protected areas.")
        @Config.RequiresMcRestart
        public boolean patchIC2Explosion = false;

        @Config.Comment("Patch explosion of Chaos Crystal in DraconicEvolution can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchDraconicEvolutionEntity = false;

        @Config.Comment("Patch explosion of RebornCore can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchRebornCoreExplosion = false;

        @Config.Comment("Patch Spatial Pylon of Applied Energistics 2 with more config.")
        @Config.RequiresMcRestart
        public boolean patchAppliedEnergistics2SpatialPylon = false;

        @Config.Comment("Blacklisted Spatial Pylon entity registry names")
        @Config.RequiresMcRestart
        public String[] spatialPylonEntityBlacklist = new String[]{"minecraft:ender_dragon"};

        @Config.Comment("Whether to invert spatial pylon entity blacklist to whitelist")
        @Config.RequiresMcRestart
        public boolean invertSpatialPylonEntityBlacklist = false;

        @Config.Comment("Patch TechGuns explosion can bypass protection.")
        @Config.RequiresMcRestart
        public boolean patchTechGunsExplosion = false;

        @Config.Comment("Patch modifiers of InfernalMobs can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchInfernalMobsModifier = false;

        @Config.Comment("Patch mob ais of Epic Siege Mod can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchEpicSiegeModAi = false;

        @Config.Comment("Disable recipe of Blackhole Controller (Deprecated) in Industrial Foregoing")
        @Config.RequiresMcRestart
        public boolean disableIndustrialForegoingBlackholeControllerRecipe = false;
    }
    @Config.Comment("Configuration for HybridFix built-in bukkit plugin.")
    @Config.RequiresMcRestart
    public static BukkitPluginSettings bukkitPluginConfig = new BukkitPluginSettings();

    public static class BukkitPluginSettings {
        @Config.Comment("Enable HybridFix built-in bukkit plugin.\n***All bukkit plugin features in this section won't work if you disabled this!***")
        @Config.RequiresMcRestart
        public boolean enable = false;

        @Config.Comment("Enable HybridFix built-in AntiExplode.")
        @Config.RequiresMcRestart
        public boolean antiExplode = false;

        @Config.Comment("Enable HybridFix Residence hook.")
        @Config.RequiresMcRestart
        public boolean hookResidence = false;

        @Config.Comment("Automatically add mod blocks to Residence config.")
        @Config.RequiresMcRestart
        public boolean autoAddModBlocksToResidenceConfig = false;

        @Config.Comment("Enable HybridFix WorldGuard hook.")
        @Config.RequiresMcRestart
        public boolean hookWorldGuard = false;

        @Config.Comment("Worlds that AntiExplode should protect.")
        @Config.RequiresMcRestart
        public String[] antiExplodeWorlds = new String[]{"world", "DIM-1", "DIM1"};
    }

    static {
        ConfigAnytime.register(Settings.class);
    }
}
