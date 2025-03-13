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
- Fix trait effects in Tinkers' Construct can bypass damage protection
- Fix So Many Enchantments can disarm players in protected areas
- Fix Botania Ring of Loki can bypass grief protection
- Fix Botania MineLens can bypass grief protection
- Fix Industrial Craft 2 miners can break blocks in protected areas
- Fix Industrial Craft 2 explosives can break blocks in protected areas
- Fix Draconic Evolution ChaosCrystal can break blocks in protected areas
- Fix TechReborn(RebornCore) explosions can break blocks in protected areas
- Offer events to Applied Energistics 2 Spatial Pylon to prevent some unpermitted griefing

## Features

- Auto override Mohist's crappy built-in explosion handling with our own method.
- Bridge Forge permission processing to Bukkit.
- Skip firing event if no listeners registered.
- Disable Timings for less performance overhead.
- Compatibility first, shouldn't break any mods/plugins.
- Built Bukkit plugin into the mod, offers utilities to server owners.
- Enhance compatibility with mod FakePlayers.
- Offer more useful apis for plugin developers to interact with Forge mods easily.
- General CraftBukkit performance improvements.

Configuration file is under `${minecraftDir}/config/hybridfix.cfg`

## Plugin Hooks
Thanks to HybridFix internal plugin, we can hook into plugins from Forge side to provide more fixes.

Currently patched plugins:
- Residence[(SpigotMC)](https://www.spigotmc.org/resources/residence-1-7-10-up-to-1-21.11480/)[(GitHub)](https://github.com/Zrips/Residence)
- WorldGuard[(BukkitDev)](https://dev.bukkit.org/projects/worldguard)[(GitHub)](https://github.com/EngineHub/WorldGuard)

## Commands

- `/hybridfix dumpitem` - Show details of the item in hand.
- `/hybridfix version` - Show version info.

## Permissions

- `hybridfix.command.use` - Allow to access `/hybridfix` command.

**Note**: Commands and permissions are registered on Bukkit side, that means you can manage permissions with Bukkit permission plugins like LuckPerms.