package io.wdsj.hybridfix.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.common.config.Config;

@Config(modid = HybridFix.MOD_ID)
public class Settings {
    @Config.Comment("(Server) Check for updates on startup.")
    @Config.Name("Check for updates")
    @Config.RequiresMcRestart
    public static boolean checkForUpdates = true;

    @Config.Comment("(Server) Re-gather capabilities on respawn, will fix Simple Difficulty(And other similar mods) thirst not getting reset on respawn.\nAnd fix dupe bug in The Betweenlands.")
    @Config.Name("Fix capability reset")
    @Config.RequiresMcRestart
    public static boolean fixCapabilityReset = true;

    @Config.Comment("(Server) Pass explosion detonate event to Bukkit. (Recommended)")
    @Config.Name("Pass explosion detonate event to Bukkit")
    @Config.RequiresMcRestart
    public static boolean passExplosionEventToBukkit = !Utils.isMohist;

    @Config.Comment("(Server) Pass explosion start event to Bukkit. (Only enable this if you are running into some compatibility issues)")
    @Config.Name("Pass explosion start event to Bukkit")
    @Config.RequiresMcRestart
    public static boolean passExplosionStartEventToBukkit = false;

    @Config.Comment("(Server) Remove entity damage & velocity on explosion being cancelled.")
    @Config.Name("Remove entity damage & velocity on cancel")
    @Config.RequiresMcRestart
    public static boolean removeEntityDamageAndVelocityOnCancel = true;

    @Config.Comment("(Server) Override Mohist's crappy explosion handling with HybridFix's.")
    @Config.Name("Override Mohist's explosion handling")
    @Config.RequiresMcRestart
    public static boolean overrideMohistExplosionHandling = Utils.isMohist;

    @Config.Comment("(Server) Bridge Forge permissions to Bukkit.")
    @Config.Name("Bridge Forge permissions to Bukkit")
    @Config.RequiresMcRestart
    public static boolean bridgeForgePermissionsToBukkit = !Utils.isMohist;

    @Config.Comment("(Server) Skip firing event if no listeners registered.")
    @Config.Name("Skip firing event if no listeners")
    @Config.RequiresMcRestart
    public static boolean skipEventIfNoListeners = true;

    @Config.Comment("(Server) Enable HybridFix's CraftServer optimizations.")
    @Config.Name("Enable CraftServer optimizations")
    @Config.RequiresMcRestart
    public static boolean enableCraftServerOptimizations = true;

    @Config.Comment("(Server) Disable Spigot's built-in Timings to save performance. (Only support Timings v1)")
    @Config.Name("Disable Timings")
    @Config.RequiresMcRestart
    public static boolean disableTimings = false;

    @Config.Comment("(Server) Register HybridFix commands.")
    @Config.Name("Register HybridFix commands")
    @Config.RequiresMcRestart
    public static boolean registerHybridFixCommands = true;

    @Config.Comment("(Server) Inject PluginClassLoader to make Forge mods can call Bukkit plugins, requires modified version of LaunchWrapper.")
    @Config.Name("Forge mods can call Bukkit plugins")
    @Config.RequiresMcRestart
    public static boolean forgeModCallBukkitPlugin = false;

    @Config.Comment("(Server) Fix Spigot EntityActivationRange for mod entities.")
    @Config.Name("Fix EntityActivationRange")
    @Config.RequiresMcRestart
    public static boolean fixEntityActivationRange = false;

    @Config.Comment("(Server) Invert EAR whitelist to blacklist.")
    @Config.Name("Invert EAR whitelist")
    @Config.RequiresMcRestart
    public static boolean invertEntityActivationRangeWhitelist = false;

    @Config.Comment("(Server) Entity ActivationRange whitelist, only mod entities IN the list will be affected by EAR.")
    @Config.Name("Entity ActivationRange whitelist")
    @Config.RequiresMcRestart
    public static String[] entityActivationRangeWhitelist = new String[]{};

    @Config.Comment("(Server) Enable HybridFix's bStats metrics.")
    @Config.Name("Enable metrics")
    @Config.RequiresMcRestart
    public static boolean enableMetrics = true;

    @Config.Comment("(Server) FakePlayer event blacklist, plugins in this list will NOT receive events fired by fake players.")
    @Config.Name("FakePlayer plugin blacklist")
    @Config.RequiresMcRestart
    public static String[] fakePlayerPluginBlacklist = new String[]{};

    @Config.Comment("(Server) Whether to invert fake player blacklist to whitelist")
    @Config.Name("Invert fake player blacklist")
    @Config.RequiresMcRestart
    public static boolean invertFakePlayerBlacklist = false;

    @Config.Comment("(Server) More Bukkit API implementation from Paper and modern versions.")
    @Config.Name("Extra Bukkit API")
    @Config.RequiresMcRestart
    public static boolean extraBukkitApi = false;

    @Config.Comment("(Server) SDK integration for RaytraceAntiXray.")
    @Config.Name("RaytraceAntiXray SDK")
    @Config.RequiresMcRestart
    public static boolean rayTraceAntiXraySDK = false;

    @Config.Comment("(Server) Deobfuscate stacktrace when Censored ASM is installed.")
    @Config.Name("Deobfuscate stacktrace")
    @Config.RequiresMcRestart
    public static boolean deobfuscateStacktrace = false;

    @Config.Comment("(Server) Don't create TE snapshot when firing InventoryMoveItemEvent\nRequires feature Extra Bukkit API.")
    @Config.Name("Dont create TE snapshot for InventoryMoveItemEvent")
    @Config.RequiresMcRestart
    public static boolean dontCreateTESnapshotForInventoryMoveItemEvent = false;

    @Config.Comment("(Server) Compatibility mode for AttackBridge.")
    @Config.Name("Compatibility mode for AttackBridge")
    @Config.RequiresMcRestart
    public static boolean compatModeForAttackBridge = false;

    @Config.Comment("(Server) Fixes for Forge and Bukkit chunk systems.")
    @Config.Name("Fixes for Forge and Bukkit chunk systems")
    @Config.RequiresMcRestart
    public static boolean fixesForForgeAndBukkitChunkSystems = true;

    @Config.Comment("(Server) Configuration for error recovery.")
    @Config.Name("Error Recovery Settings")
    @Config.RequiresMcRestart
    public static ErrorRecoverySettings errorRecoverySettings = new ErrorRecoverySettings();

    public static class ErrorRecoverySettings {
        @Config.Comment("(Server) Enable error recovery.")
        @Config.Name("Enable error recovery")
        public boolean enable = false;

        @Config.Comment("(Server) Whether to broadcast message to players on a recovery.")
        @Config.Name("Broadcast message")
        @Config.RequiresMcRestart
        public boolean broadcastMessage = true;

        @Config.Comment("(Server) Permission needed for receiving the message. (Set to empty to send to all players)")
        @Config.Name("Message permission")
        @Config.RequiresMcRestart
        public String messagePermission = "hybridfix.recovery.message";

        @Config.Comment("(Server) Message to send.")
        @Config.Name("Message")
        @Config.RequiresMcRestart
        public String message = "&cServer threw exception in %error_point%, now has recovered.";
    }

    @Config.Comment("(Server) Configuration for packet-related stuff.")
    @Config.Name("Packet Settings")
    @Config.RequiresMcRestart
    public static PacketSettings packetSettings = new PacketSettings();

    public static class PacketSettings {
        @Config.Comment("(Server) How many items can be dropped per tick?\nThis modifies CraftBukkit's packet limiter. Leave the value as 20 to prevent applying this mixin.")
        @Config.Name("Max dropped items per tick")
        @Config.RequiresMcRestart
        public int maxDroppedItemsPerTick = 20;
    }

    @Config.Comment("(Server) Configuration for HybridFix mod patches.")
    @Config.Name("Mod Patch Settings")
    @Config.RequiresMcRestart
    public static ModPatchSettings modPatchSettings = new ModPatchSettings();

    public static class ModPatchSettings {
        @Config.Comment("(Server / Client) Patch default config values of Actually Additions.")
        @Config.RequiresMcRestart
        public boolean patchActuallyAdditionsConfig = true;

        @Config.Comment("(Server) Patch saplings in twilight forest bypass Bukkit grief protections exploit.")
        @Config.RequiresMcRestart
        public boolean patchTwilightForestSapling = true;

        @Config.Comment("(Server) Patch entities in twilight forest (e.g. Naga) bypass Bukkit grief protections exploit.")
        @Config.RequiresMcRestart
        public boolean patchTwilightForestEntityEvent = true;

        @Config.Comment("(Server) Patch items in twilight forest (e.g. MagicBeans) bypass Bukkit grief protections exploit.")
        @Config.RequiresMcRestart
        public boolean patchTwilightForestItem = true;

        @Config.Comment("(Server) Patch taint in thaumcraft spread event.")
        @Config.RequiresMcRestart
        public boolean patchThaumcraftTaintSpread = true;

        @Config.Comment("(Server) Patch flux rift in thaumcraft.")
        @Config.RequiresMcRestart
        public boolean patchThaumcraftFlux = true;

        @Config.Comment("(Server) Patch Tinkers Construct tool damage,\nUseful for some traits that adds extra effects and disarming, etc.\nYou can use property -Dhybridfix.tconstruct.supersedeVanillaEvent=true to prevent calling original damage event.")
        @Config.RequiresMcRestart
        public boolean patchTconstructToolDamage = true;

        @Config.Comment("(Server) Patch Disarm enchantment in SME(1.0.0 and higher), prevent bypassing the bukkit protection.")
        @Config.RequiresMcRestart
        public boolean patchSoManyEnchantmentsDisarm = true;

        @Config.Comment("(Server) Patch lens of Botania can bypass bukkit grief protection.")
        @Config.RequiresMcRestart
        public boolean patchBotaniaLens = true;

        @Config.Comment("(Server) Patch Rannuncarpus can bypass bukkit grief protection.\nNOTE: This fix uses FakePlayer!")
        @Config.RequiresMcRestart
        public boolean patchBotaniaBlock = true;

        @Config.Comment("(Server) Patch miner machines of Industrial Craft 2 can mine blocks in protected areas.\nNOTE: This fix uses FakePlayer!")
        @Config.RequiresMcRestart
        public boolean patchIC2Machine = true;

        @Config.Comment("(Server) Patch explosions of Industrial Craft 2 can break blocks in protected areas.")
        @Config.RequiresMcRestart
        public boolean patchIC2Explosion = true;

        @Config.Comment("(Client) Replace reflection operations of Industrial Craft 2 AudioManager with our faster one.")
        @Config.RequiresMcRestart
        public boolean patchIC2AudioManager = true;

        @Config.Comment("(Server) Patch explosion of Chaos Crystal in DraconicEvolution can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchDraconicEvolutionEntity = true;

        @Config.Comment("(Server) Patch explosion of RebornCore can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchRebornCoreExplosion = true;

        @Config.Comment("(Server) Patch Spatial Pylon of Applied Energistics 2 with more config.")
        @Config.RequiresMcRestart
        public boolean patchAppliedEnergistics2SpatialPylon = true;

        @Config.Comment("(Server) Blacklisted Spatial Pylon entity registry names")
        @Config.RequiresMcRestart
        public String[] spatialPylonEntityBlacklist = new String[]{"minecraft:ender_dragon"};

        @Config.Comment("(Server) Whether to invert spatial pylon entity blacklist to whitelist")
        @Config.RequiresMcRestart
        public boolean invertSpatialPylonEntityBlacklist = false;

        @Config.Comment("(Server) Patch TechGuns explosion can bypass protection.")
        @Config.RequiresMcRestart
        public boolean patchTechGunsExplosion = true;

        @Config.Comment("(Server) Patch modifiers of InfernalMobs can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchInfernalMobsModifier = true;

        @Config.Comment("(Server) Patch mob ais of Epic Siege Mod can bypass grief protection.")
        @Config.RequiresMcRestart
        public boolean patchEpicSiegeModAi = true;

        @Config.Comment("(Server & Client) Disable recipe of Blackhole Controller (Deprecated) in Industrial Foregoing")
        @Config.RequiresMcRestart
        public boolean disableIndustrialForegoingBlackholeControllerRecipe = false;

        @Config.Comment("(Server) Patch Witchery symbol effects to make them safer to use.")
        @Config.RequiresMcRestart
        public boolean patchWitcherySymbolEffect = true;
    }

    @Config.Comment("Configuration for HybridFix ASM plugin patcher.")
    @Config.RequiresMcRestart
    public static PluginPatcherSettings pluginPatcherSettings = new PluginPatcherSettings();

    public static class PluginPatcherSettings {
        @Config.Comment("Enable HybridFix ASM plugin patcher.")
        @Config.RequiresMcRestart
        public boolean enable = false;

        @Config.Comment("Enable HybridFix ASM plugin patcher for Residence 6.0+.")
        @Config.RequiresMcRestart
        public boolean patchResidenceV6 = false;
    }

    @Config.Comment("(Server) Configuration for HybridFix built-in bukkit plugin.")
    @Config.Name("Bukkit Plugin Settings")
    @Config.RequiresMcRestart
    public static BukkitPluginSettings bukkitPluginConfig = new BukkitPluginSettings();

    public static class BukkitPluginSettings {
        @Config.Comment("(Server) Enable HybridFix built-in bukkit plugin.\n***All bukkit plugin features in this section won't work if you disabled this!***")
        @Config.Name("Enable Plugin")
        @Config.RequiresMcRestart
        public boolean enable = false;

        @Config.Comment("(Server) Enable HybridFix built-in AntiExplode.")
        @Config.Name("Anti explode")
        @Config.RequiresMcRestart
        public boolean antiExplode = false;

        @Config.Comment("(Server) Enable HybridFix Residence hook.")
        @Config.Name("Hook Residence")
        @Config.RequiresMcRestart
        public boolean hookResidence = false;

        @Config.Comment("(Server) Automatically add mod blocks to Residence config.")
        @Config.Name("Auto add mod blocks to Residence config")
        @Config.RequiresMcRestart
        public boolean autoAddModBlocksToResidenceConfig = false;

        @Config.Comment("(Server) Enable HybridFix WorldGuard hook.")
        @Config.Name("Hook WorldGuard")
        @Config.RequiresMcRestart
        public boolean hookWorldGuard = false;

        @Config.Comment("(Server) Enable HybridFix Citizens hook.")
        @Config.Name("Hook Citizens")
        @Config.RequiresMcRestart
        public boolean hookCitizens = false;

        @Config.Comment("(Server) Worlds that AntiExplode should protect.")
        @Config.Name("AntiExplode worlds")
        @Config.RequiresMcRestart
        public String[] antiExplodeWorlds = new String[]{"world", "DIM-1", "DIM1"};
    }

    @Config.Comment("(Server) Configuration for HybridFix debugger.")
    @Config.Name("Debug Settings")
    @Config.RequiresMcRestart
    public static DebugSettings debugSettings = new DebugSettings();

    public static class DebugSettings {
        @Config.Comment("(Server) Enable HybridFix entity health debugger.")
        @Config.Name("Entity health debugger")
        @Config.RequiresMcRestart
        public boolean entityHealthDebugger = false;
    }

    static {
        ConfigAnytime.register(Settings.class);
    }
}
