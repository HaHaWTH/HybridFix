# HybridFix (混合修复)

Provide bugfixes, optimizations and utilities for Forge+Bukkit server environments.

## Fixed issues

### Forge-Bukkit

- Some mod explosions cannot be handled by Bukkit plugins(e.g. Tinkers' Construct EFLN)

### Mods

- Simple Difficulty, ToughAsNails(And any other similar mods) thirst is not getting reset on player respawn[(Luohuayu/CatServer#536)](https://github.com/Luohuayu/CatServer/issues/536)[(MohistMC/Mohist#2905)](https://github.com/MohistMC/Mohist/issues/2905)
- Ring dupe bug in The Betweenlands mod[(Luohuayu/CatServer#204)](https://github.com/Luohuayu/CatServer/issues/204)
- Simulate vanilla player respawn, most dupe bugs on player death should be fixed
- Fix Twilight Forest saplings can bypass anti-grief plugin protection bug
- Fix Twilight Forest entities(e.g. Naga) can break blocks in protected areas
- Fix Twilight Forest Chain with Block can break blocks in protected areas
- Fix Thaumcraft 6 taint can spread into protected areas
- Fix Thaumcraft 6 flux rift can break blocks in protected areas
- Fix trait effects in Tinkers' Construct can bypass damage protection
- Fix So Many Enchantments can disarm players in protected areas
- Fix Botania Ring of Loki can bypass grief protection
- Fix Botania MineLens can bypass grief protection
- Fix Industrial Craft 2 miners can break blocks in protected areas
- Fix Industrial Craft 2 explosives can break blocks in protected areas
- Fix Draconic Evolution ChaosCrystal can break blocks in protected areas
- Fix TechReborn(RebornCore) explosions can break blocks in protected areas
- Offer events to Applied Energistics 2 Spatial Pylon to prevent some unpermitted griefing
- Fix TechGuns explosion can break blocks in protected areas
- Add entity blacklist/whitelist to Applied Energistics 2 Spatial Pylon
- Fix webs of InfernalMobs can spawn in protected areas
- Fix EpicSiegeMod mob AIs can grief in protected areas

## Features

- Auto override Mohist's crappy built-in explosion handling with our own method.
- Bridge Forge permission processing to Bukkit.
- Skip firing event if no listeners registered.
- Disable Timings for less performance overhead.
- Compatibility first, shouldn't break any mods/plugins.
- Built Bukkit plugin into the mod, offers utilities to server owners.
- Enhance compatibility with mod FakePlayers.
- Offer more useful apis for plugin developers to interact with Forge mods easily.
- Extensive APIs for mod developers to maintain compatibility with hybrid servers easily.
- General CraftBukkit performance improvements.

Configuration file is under `${minecraftDir}/config/hybridfix.cfg`

## How To (Server Admins)

Download HybridFix and its dependencies(MixinBooter and ConfigAnyTime) from CurseForge, drop HybridFix into `${minecraftDir}/mods` folder.

HybridFix is not required to be installed on client side.

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

APIs are located in `io.wdsj.hybridfix.api` package.

## Plugin Hooks
Thanks to HybridFix internal plugin, we can hook into plugins from Forge side to provide more fixes.

Currently patched plugins:
- Residence[(SpigotMC)](https://www.spigotmc.org/resources/residence-1-7-10-up-to-1-21.11480/)[(GitHub)](https://github.com/Zrips/Residence)
- WorldGuard[(BukkitDev)](https://dev.bukkit.org/projects/worldguard)[(GitHub)](https://github.com/EngineHub/WorldGuard)

## Commands

- `/hybridfix dumpitem` - Show details of the item in hand.
- `/hybridfix version` - Show version info.
- `/hybridfix eraseentity` - Forcibly remove targeted entity.

## Permissions

- `hybridfix.command.use` - Allow to access `/hybridfix` command.
- `hybridfix.command.eraseentity.use` - Allow to access `/hybridfix eraseentity` command.

**Note**: Commands and permissions are registered on Bukkit side, that means you can manage permissions with Bukkit permission plugins like LuckPerms.