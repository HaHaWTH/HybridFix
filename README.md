# HybridFix

Provide bugfixes for Forge+Bukkit server environments.

## Fixed issues

### Forge-Bukkit

- Some mod explosions cannot be handled by Bukkit plugins(e.g. Tinkers' Construct EFLN)

### Mods

- Simple Difficulty, ToughAsNails(And any other similar mods) thirst is not getting reset on player respawn[(Luohuayu/CatServer#536)](https://github.com/Luohuayu/CatServer/issues/536)[(MohistMC/Mohist#2905)](https://github.com/MohistMC/Mohist/issues/2905)

## Implemented Features

- Auto override Mohist's crappy built-in explosion handling with our own method.
- Bridge Forge permission processing to Bukkit.
