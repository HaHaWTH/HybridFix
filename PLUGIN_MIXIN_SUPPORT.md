## Bukkit Plugin Mixin Support (Experimental)

HybridFix provides an experimental feature that allows Forge mods to apply **Mixins** directly to Bukkit plugins.
Since Bukkit plugins run in their own isolated `PluginClassLoader`, standard Mixin configurations cannot target them. HybridFix bridges this gap by injecting a transformer into the plugin loading process.

### Prerequisites

To use this feature, **you MUST replace the server bootstrap** (LaunchWrapper, Bouncepad, etc.) with a modified version supported by HybridFix. This is required because the standard Mixin service is locked down during startup, and HybridFix needs deep access to inject transformers into the plugin loader.

Supported bootstraps:
- [LegacyLauncher/LaunchWrapper (Forge hybrid server software)](https://github.com/HaHaWTH/LegacyLauncher)
- [Bouncepad (CatRoom 0.1.0)](https://github.com/HaHaWTH/Bouncepad)
- [Foundation (CatRoom 0.2.0 and later)(Experimental)](https://github.com/HaHaWTH/Foundation)

### Enabling the Feature

After replacing the server bootstrap, you must enable this feature in the HybridFix configuration file (`hybridfix.cfg`):

```cfg
# (Server) Configuration for HybridFix ASM plugin patcher.
plugin_patcher_settings {
    # (Server) Enable HybridFix ASM plugin patcher.
    B:enable=true
    # (Server) (EXPERIMENTAL) Enable HybridFix plugin Mixin support. (Requires modified bootstrapping service)
    B:enableMixin=true
}
```

### Developer API

To inject Mixins into a Bukkit plugin from your Forge mod, follow these steps:

- Add Dependency
Ensure your mod depends on HybridFix. You can do this via build.gradle (see README) or by checking if the class exists at runtime.
- Listen to BukkitMixinSetupEvent
Register an event listener. This event is fired when HybridFix is ready to accept Mixin configurations for plugins.
```java
import io.wdsj.hybridfix.api.forge.event.bukkit_mixin.BukkitMixinSetupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MyMixinHandler {
    @SubscribeEvent
    public void onBukkitMixinSetup(BukkitMixinSetupEvent event) {
        // Add your mixin configuration file name
        event.addMixinConfig("mixins.my_mod.plugins.json");
    }
    
    // Don't forget to register this handler in your mod's main class
}
```
- Create the Mixin Config
Create a standard Mixin JSON file (e.g., mixins.my_mod.plugins.json) in your resources.
**Important Notes**:
Remap: Ensure remap is set to false in your Mixin annotations, as Bukkit plugins are not obfuscated.
Refmap: You typically do not need a refmap for plugin mixins, or you can point to a separate one.
Example mixins.my_mod.plugins.json:
```json
{
  "package": "com.example.mymod.mixin.plugin",
  "required": true,
  "minVersion": "0.8",
  "compatibilityLevel": "JAVA_8",
  "mixins": [
    "MixinResidenceBlockListener"
  ]
}
```
- Writing the Mixin
Write your Mixin class targeting the plugin class.
```java
package com.example.mymod.mixin.plugin;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Remap must be false!
@Mixin(value = ResidenceBlockListener.class, remap = false)
public class MixinResidenceBlockListener {

    @Inject(method = "onBlockBreak", at = @At("HEAD"))
    public void onBreak(CallbackInfo ci) {
        System.out.println("Residence block break listener invoked via Mixin!");
    }
}
```