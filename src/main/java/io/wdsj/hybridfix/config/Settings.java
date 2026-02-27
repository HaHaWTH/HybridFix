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

    @Config.Comment("(Client / Server) Optimize Player and Entity tracking.")
    @Config.Name("Optimize Player and Entity tracking")
    @Config.RequiresMcRestart
    public static boolean optimizePlayerTracking = false;

    @Config.Comment("(Server) Disable Spigot's built-in Timings to save performance. (Only support Timings v1)")
    @Config.Name("Disable Timings")
    @Config.RequiresMcRestart
    public static boolean disableTimings = false;

    @Config.Comment("(Server & Client) Optimize entity AI nearest entity lookup.")
    @Config.Name("Optimize entity AI")
    @Config.RequiresMcRestart
    public static boolean optimizeEntityAI = true;

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

    @Config.Comment("(Server) Don't create TE snapshot when firing InventoryMoveItemEvent.\nRequires feature Extra Bukkit API.")
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
    public static boolean fixesForForgeAndBukkitChunkSystems = false;

    @Config.Comment("(Server) Fix 'outdated server' showing in ping before server fully boots.")
    @Config.Name("Fix outdated server")
    @Config.RequiresMcRestart
    public static boolean fixOutdatedServerPingStatus = false;

    @Config.Comment("(Client / Server) Prevent setting NaN health to entities.")
    @Config.Name("Prevent setting NaN health")
    @Config.RequiresMcRestart
    public static boolean preventSettingNaNHealth = true;

    @Config.Comment("(Server) Fix Authlib's profile lookup.")
    @Config.Name("Fix Authlib's profile lookup")
    public static boolean fixAuthLibProfileLookup = true;

    @Config.Comment("(Server) Load extra language file for dedicated servers.")
    @Config.Name("Load extra language file")
    public static boolean extraLanguageFile = false;

    @Config.Comment("(Server) Preferred language for extra lang file.")
    @Config.Name("Preferred language")
    public static String preferredLanguage = "en_us:en_US";

    @Config.Comment("(Server) Startup MOTD settings. (Only works when 'Fix outdated server' is enabled)")
    @Config.Name("Startup MOTD Settings")
    @Config.RequiresMcRestart
    public static StartUpMOTDSettings startUpMOTDSettings = new StartUpMOTDSettings();

    public static class StartUpMOTDSettings {
        @Config.Comment("(Server) Whether to enable startup MOTD.")
        @Config.Name("Enable startup MOTD")
        @Config.RequiresMcRestart
        public boolean enable = true;

        @Config.Comment("(Server) MOTD to show when server starts. (Format code supported)")
        @Config.Name("MOTD message")
        @Config.RequiresMcRestart
        public String messageOfTheDay = "&cServer is still starting!\nPlease wait before reconnecting.";
    }

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
        public String message = "&c%error_point%, now has recovered.";
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

    @Config.Comment("(Server & Client) Configuration for HybridFix mod patches.")
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

        @Config.Comment("(Server & Client) Patch fragile parts of Quark ASM (Behavior may slightly change).")
        @Config.RequiresMcRestart
        public boolean patchQuarkASM = HybridFix.IS_HYBRID_ENV;

        @Config.Comment("(Server) Patch taint in thaumcraft spread event.")
        @Config.RequiresMcRestart
        public boolean patchThaumcraftTaintSpread = true;

        @Config.Comment("(Server) Patch flux rift in thaumcraft.")
        @Config.RequiresMcRestart
        public boolean patchThaumcraftFlux = true;

        @Config.Comment("(Server) Patch Tinkers Construct tool damage,\nUseful for some traits that adds extra effects and disarming, etc.\nYou can use property -Dhybridfix.tconstruct.supersedeVanillaEvent=true to prevent calling original damage event.")
        @Config.RequiresMcRestart
        public boolean patchTconstructToolDamage = true;

        @Config.Comment("(Server / Client) Fix TConstruct crash bug due to the network design flaw.")
        @Config.RequiresMcRestart
        public boolean patchTConstructNetworkCrash = true;

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

        @Config.Comment("(Server) Patch Mekanism Digital Miner to prevent mining TEs.")
        @Config.RequiresMcRestart
        public boolean patchMekanismDigitalMiner = true;

        @Config.Comment("(Client / Server) Patch entity control logic of Aether Legacy.")
        @Config.RequiresMcRestart
        public boolean patchAetherLegacyEntityControl = true;

        @Config.Comment("(Client & Server) Make Swets dissolve slowly like modern versions.")
        @Config.RequiresMcRestart
        public boolean aetherLegacySwetsDissolveSlowly = false;

        @Config.Comment("(Client / Server) Patch unsafe calls in Aether Legacy.")
        @Config.RequiresMcRestart
        public boolean patchAetherLegacyUnsafeCall = true;

        @Config.Comment("(Client) Enable VoxelMap Residence support, drawing nearby residences on the map.")
        @Config.RequiresMcRestart
        public boolean voxelMapResidenceSupport = false;

        @Config.Comment("(Client) Patch VoxelMap teleport command.")
        @Config.RequiresMcRestart
        public boolean patchVoxelMapTeleportCommand = true;

        @Config.Comment("(Server) Enable JEID Bukkit support.")
        @Config.RequiresMcRestart
        public boolean patchJEIDBukkitSupport = true;
    }

    @Config.Comment("(Server) Configuration for HybridFix ASM plugin patcher.")
    @Config.Name("Plugin Patcher Settings")
    @Config.RequiresMcRestart
    public static PluginPatcherSettings pluginPatcherSettings = new PluginPatcherSettings();

    public static class PluginPatcherSettings {
        @Config.Comment("(Server) Enable HybridFix ASM plugin patcher.")
        @Config.RequiresMcRestart
        public boolean enable = false;

        @Config.Comment("(Server) (EXPERIMENTAL) Enable HybridFix plugin Mixin support. (Requires modified bootstrapping service)")
        @Config.RequiresMcRestart
        public boolean enableMixin = false;

        @Config.Comment("(Server) Enable HybridFix ASM plugin patcher for Residence 6.0+.")
        @Config.RequiresMcRestart
        public boolean patchResidenceV6 = false;

        @Config.Comment("(Server) Enable HybridFix ASM plugin patcher for InventoryView compatibility.")
        @Config.RequiresMcRestart
        public boolean patchInventoryViewInsn = true;

        @Config.Comment("(Server) Plugins that should be patched by InventoryView patcher.")
        @Config.RequiresMcRestart
        public String[] patchInventoryViewPlugins = new String[]{"fairy-lib-plugin"};

        @Config.Comment("(Server) Enable HybridFix ASM plugin patcher for MapPalette.")
        @Config.RequiresMcRestart
        public boolean enableMapPalettePatch = true;

        @Config.Comment("(Server) Plugins that should be patched by MapPalette patcher.")
        @Config.RequiresMcRestart
        public String[] mapPalettePatchPlugins = new String[]{};

        @Config.Comment("(Server) Patch reflection operations of plugins on Cleanroom.")
        @Config.RequiresMcRestart
        public boolean patchReflectField = true;

        @Config.Comment("(Server) Plugins that should be patched by reflection field patcher.")
        @Config.RequiresMcRestart
        public String[] patchReflectFieldPlugins = new String[]{};
    }

    @Config.Comment("(Server / Client) Configuration for HybridFix ASM mod patcher.")
    @Config.Name("ASM Mod Patcher Settings")
    @Config.RequiresMcRestart
    public static ASMModPatcherSettings asmModPatcherSettings = new ASMModPatcherSettings();

    public static class ASMModPatcherSettings {
        @Config.Comment("(Client / Server) Enable HybridFix ASM mod patcher.")
        @Config.RequiresMcRestart
        public boolean enable = false;

        @Config.Comment("(Client / Server) Remove instanceof FakePlayer checks in PlayerEvent.Clone listeners.")
        @Config.RequiresMcRestart
        public boolean removeFakePlayerInstOf = false;

        @Config.Comment("(Client / Server) Classes that instanceof FakePlayer removal should target.")
        @Config.RequiresMcRestart
        public String[] removeFakePlayerInstOfClasses = new String[]{"com.othermod.core.PlayerEventHandler"};

        @Config.Comment("(Client / Server) Clear specified method and return the value.")
        @Config.RequiresMcRestart
        public boolean methodNoOpPatcher = true;

        @Config.Comment("(Client / Server) Targets of method no op patcher should apply.")
        @Config.RequiresMcRestart
        public String[] methodNoOpPatcherTargets = new String[]{
                "com.othermod.core.PlayerEventHandler|doStuff(Z)I|114514",
                "com.example.config.UpdaterClass|getUpdate()Ljava/lang/String;|It's MyGO!!!!!",
                "blusunrize.immersiveengineering.ImmersiveEngineering$ThreadContributorSpecialsDownloader|run()V",
                "hellfirepvp.astralsorcery.common.base.patreon.PatreonDataManager|loadPatreonEffects()V",
                "vazkii.quark.base.client.ContributorRewardHandler|init()V",
                "vazkii.quark.base.client.ContributorRewardHandler|onRenderPlayer",
                "vazkii.quark.base.client.ContributorRewardHandler|onPlayerJoin",
                "vazkii.quark.base.client.ContributorRewardHandler$ThreadContributorListLoader|run()V",
                "com.buuz135.industrial.proxy.CommonProxy|readUrl|{\"uuid\":[]}",
                "com.brandon3055.draconicevolution.handlers.ContributorHandler|init()V",
                "com.brandon3055.draconicevolution.handlers.ContributorHandler|isPlayerContributor|false",
                "com.brandon3055.draconicevolution.handlers.ContributorHandler|onPlayerLogin",
                "com.brandon3055.draconicevolution.handlers.ContributorHandler$DLThread|run()V",
                "com.brandon3055.draconicevolution.handlers.ContributorHandler|loadContributorConfig()V",
                "com.brandon3055.draconicevolution.handlers.ContributorHandler|saveContributorConfig()V",
                "org.cyclops.cyclopscore.tracking.ImportantUsers|checkAll()V",
                "org.cyclops.cyclopscore.tracking.Analytics|sendAll()V",
                "org.cyclops.cyclopscore.tracking.Versions|checkAll()V",
                "org.cyclops.cyclopscore.tracking.Versions|onTick",
                "vazkii.botania.common.core.version.VersionChecker|init()V",
                "vazkii.botania.common.core.version.VersionChecker|onTick",
                "vazkii.botania.common.core.version.ThreadVersionChecker|run()V",
                "vazkii.botania.common.core.version.ThreadDownloadMod|run()V",
                "vazkii.botania.common.core.version.ThreadDownloadMod|sendError()V",
                "shadows.placebo.patreon.PatreonManager|init",
                "shadows.placebo.patreon.PatreonManager|onPlayerTick"
        };
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

        @Config.Comment("(Server) Send server Residence data to client for VoxelMap display feature.")
        @Config.Name("Send client Residence data")
        public boolean sendClientResidenceData = true;

        @Config.Comment("(Server) Send server world info to client for VoxelMap.")
        @Config.Name("Send VoxelMap client world info")
        public boolean sendClientWorldInfo = false;

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
