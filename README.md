# HybridFix (混合修复)

Provide bugfixes, optimizations and utilities for Forge+Bukkit server environments.

## Fixed issues

### Forge-Bukkit

- Some mod explosions cannot be handled by Bukkit plugins(e.g. Tinkers' Construct EFLN)

### Mods

- Simple Difficulty, ToughAsNails(And any other similar mods) thirst is not getting reset on player respawn[(Luohuayu/CatServer#536)](https://github.com/Luohuayu/CatServer/issues/536)[(MohistMC/Mohist#2905)](https://github.com/MohistMC/Mohist/issues/2905)

## Features

- Auto override Mohist's crappy built-in explosion handling with our own method.
- Bridge Forge permission processing to Bukkit.
- Skip firing event if no listeners registered.
- Disable Timings for less performance overhead.
- Compatibility first, shouldn't break any mods/plugins.
- Built Bukkit plugin into the mod, offers utilities to server owners.
- General CraftBukkit performance improvements.

Configuration file is under `${minecraftDir}/config/hybridfix.cfg`

## Commands

- `/hybridfix dumpitem` - Show details of the item in hand.
- `/hybridfix version` - Show version info.

## Permissions

- `hybridfix.command.use` - Allow to access `/hybridfix` command.

**Note**: Commands and permissions are registered on Bukkit side, that means you can manage permissions with Bukkit permission plugins like LuckPerms.