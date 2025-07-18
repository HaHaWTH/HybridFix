## Forge mods access Bukkit plugins

HybridFix provides a working way to access Bukkit plugins from Forge side,

To use this, you need to replace server bootstrap (LaunchWrapper, Bouncepad, etc.) with HybridFix's one:

- [LegacyLauncher/LaunchWrapper](https://github.com/HaHaWTH/LegacyLauncher)
- [Bouncepad](https://github.com/HaHaWTH/Bouncepad)

After replacing server bootstrap, you need to enable it via config option `Forge mods can call Bukkit plugins` as well.

When completed, you should see `Forge to Bukkit access now enabled.` message in your console.

### API

To access Bukkit plugins safely, and work on servers without Forge call Bukkit enabled, you need to use methods provided by ListenerHackery:

```java
public class MyHandler {
    public void onEvent(Event event) {
        if (ListenerHackery.ensureSafeAccess("PluginName")) {
            doSomething();
        }
    }
}
```

**Note:** Accessing Bukkit plugins before `FMLServerStartedEvent` fired will never work, `ListenerHackery.ensureSafeAccess` will always return false.