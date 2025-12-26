# HybridFix (混合修复)

Provide a bunch of bugfixes, optimizations and utilities for Forge+Bukkit server environments.

## Fixed issues

### Forge-Bukkit

- Bukkit plugins cannot handle some mod explosions(e.g., Tinkers' Construct EFLN)
- Bridge some Forge events to Bukkit

### Mods

#### Client

- Replace expensive reflection usages in Industrial Craft 2 with direct calls

#### Server

- Simple Difficulty, ToughAsNails(And any other similar mods) thirst is not getting reset on player respawn[(Luohuayu/CatServer#536)](https://github.com/Luohuayu/CatServer/issues/536)[(MohistMC/Mohist#2905)](https://github.com/MohistMC/Mohist/issues/2905)
- Ring dupe bug in The Betweenlands mod[(Luohuayu/CatServer#204)](https://github.com/Luohuayu/CatServer/issues/204)
- Simulate vanilla player respawn, most dupe bugs on player death should be fixed
- Fix Twilight Forest saplings can bypass the anti-grief plugin protection bug
- Fix Twilight Forest entities(e.g. Naga) can break blocks in protected areas
- Fix Twilight Forest Chain with Block can break blocks in protected areas
- Fix Thaumcraft 6 taint can spread into protected areas
- Fix Thaumcraft 6 flux rift can break blocks in protected areas
- Fix trait effects in Tinkers' Construct can bypass damage protection
- Fix So Many Enchantments can disarm players in protected areas
- Fix Botania Ring of Loki can bypass grief protection
- Fix Botania MineLens can bypass grief protection
- Fix Botania Rannuncarpus can bypass grief protection
- Fix Industrial Craft 2 miners can break blocks in protected areas
- Fix Industrial Craft 2 explosives can break blocks in protected areas
- Fix Draconic Evolution ChaosCrystal can break blocks in protected areas
- Fix TechReborn(RebornCore) explosions can break blocks in protected areas
- Offer events to Applied Energistics 2 Spatial Pylon to prevent some unpermitted griefing
- Fix TechGuns explosion can break blocks in protected areas
- Add entity blacklist/whitelist to Applied Energistics 2 Spatial Pylon
- Fix webs of InfernalMobs can spawn in protected areas
- Fix EpicSiegeMod mob AIs can grief in protected areas
- Disable recipe of Blackhole Controller (Deprecated) in Industrial Foregoing
- Fix Witchery spells can bypass damage protection
- Auto modify Actually Additions' config to make it fit the server environment
- Prevent Mekanism Digital Miner from mining tile entities

## Features

- Auto overrides Mohist's crappy built-in explosion handling with our own method.
- Bridge Forge permission processing to Bukkit.
- Skip firing event if no listeners registered.
- Disable Timings for lower performance overhead.
- Compatibility first, shouldn't break any mods/plugins.
- Built Bukkit plugin into the mod, offers utilities to server owners.
- Enhance compatibility with mod FakePlayers.
- Offer more or less useful apis for plugin developers to interact with Forge mods easily.
- Extensive APIs for mod developers to maintain compatibility with hybrid servers easily.
- General CraftBukkit performance improvements.
- Auto deobfuscate plugin stacktraces when Censored ASM is installed for easier debugging.
- **Cross ClassLoader access, make Forge mods can easily interact with Bukkit plugins. ([Details](./FORGE_CALL_BUKKIT.md))**
- Bukkit APIs backported from modern version.
- **Builtin plugin ASM patcher, compatible with your favourite plugins.**
- **Bukkit plugin mixin support, allows you to inject code into plugins via mods.** *(Experimental)* ([Docs](./PLUGIN_MIXIN_SUPPORT.md))
- Extend Forge's error recovery system (`removeErroringEntities` and `removeErroringTileEntities`), notify server operators when errors occurred.
- Guard player ticking process, kick player instead of directly crash.
- Fix Forge bugs that affect server play experience.

Configuration file is under `${minecraftDir}/config/hybridfix.cfg`

## How To (Server Admins)

Download HybridFix and its dependencies(MixinBooter and ConfigAnyTime) from CurseForge, drop HybridFix into `${minecraftDir}/mods` folder.

HybridFix is not required to be installed on the client side, but some features may not work without them.

## How To (Developers)

If you are developing mods, you can import HybridFix from curse maven:

Gradle(Groovy DSL):

```groovy
implementation fg.deobf("curse.maven:hybridfix-1166614:{latest_artifact_id}")
```

If you are developing plugins, you can import HybridFix jar directly:

Maven:

```xml
<dependency>
    <groupId>io.wdsj</groupId>
    <artifactId>hybridfix</artifactId>
    <version>{latest_version}</version>
    <scope>system</scope>
    <systemPath>PATH-TO-JAR</systemPath>
</dependency>
```

Gradle(Groovy DSL):

```groovy
compileOnly files("PATH-TO-JAR")
```

APIs are located at `io.wdsj.hybridfix.api` and `io.wdsj.hybridfix.duck.api` package.

## Plugin Hooks
Thanks to HybridFix internal plugin, we can hook into plugins from Forge side to provide more fixes.

Currently patched plugins:
- Residence[(SpigotMC)](https://www.spigotmc.org/resources/residence-1-7-10-up-to-1-21.11480/)[(GitHub)](https://github.com/Zrips/Residence)
- WorldGuard[(BukkitDev)](https://dev.bukkit.org/projects/worldguard)[(GitHub)](https://github.com/EngineHub/WorldGuard)
- Citizens[(GitHub)](https://github.com/CitizensDev/Citizens2)

## Plugin ASM patcher
HybridFix uses ASM to inject into plugin classes dynamically at runtime 
to resolve plugin compatibility problems with Forge+Bukkit environment.

Currently supported plugins:
- Residence: Fixes compatibility with Residence 6.0.0.0+.

## About FakePlayers

HybridFix utilizes Forge FakePlayer to fix some grief bugs, the naming logic is `[modid-ClassName]`.

For example, the botania rannuncarpus patch is using the fake player with name `[botania-SubTileRannuncarpus]`.

You can add these names into whitelist/blacklist of your plugins to customize behavior.

## Commands

- `/hybridfix dumpitem` - Show details of the item holding in the hand.
- `/hybridfix dumpentity` - Show details of the entity targeted.
- `/hybridfix dumpblock` - Show details of the block targeted.
- `/hybridfix version` - Show version info.
- `/hybridfix eraseentity` - Forcibly remove targeted entity.
- `/mods` - Like `/plugins`, show all loaded mods.

## Permissions

- `hybridfix.command.use` - Allow to access `/hybridfix` command.
- `hybridfix.command.eraseentity.use` - Allow to access `/hybridfix eraseentity` command.
- `hybridfix.command.mods.use` - Allow to access `/mods` command.

**Note**: Commands and permissions are registered on Bukkit side, that means you can manage permissions with Bukkit permission plugins like LuckPerms.

## License

This mod is licensed under LGPL-2.1.

Backported Bukkit classes are licensed under GPL-3.0.

Unless explicitly specified in the code, other parts are under LGPL-2.1.

The stripped MCP remapped CraftBukkit JAR in the repository is for development use only and not included in releases.
