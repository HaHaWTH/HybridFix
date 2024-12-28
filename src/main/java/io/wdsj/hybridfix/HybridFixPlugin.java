package io.wdsj.hybridfix;

import com.google.common.collect.ImmutableMap;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.Utils;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static io.wdsj.hybridfix.HybridFix.IS_HYBRID_ENV;

@IFMLLoadingPlugin.Name("HybridFixPlugin")
public class HybridFixPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final boolean isClient = FMLLaunchHandler.side().isClient();

    private static final Map<String, Supplier<Boolean>> serversideMixinConfigs = ImmutableMap.copyOf(new HashMap<String, Supplier<Boolean>>()
    {
        {
            put("mixins.fix.capability.json", () -> Settings.fixCapabilityReset);
            put("mixins.fix.capability.mohist.json" , () -> Settings.fixCapabilityReset && Utils.isClassLoaded("com.mohistmc.MohistMC"));
            put("mixins.bridge.explosion.json", () -> Settings.passExplosionEventToBukkit);
            put("mixins.bridge.explosion.mohist.json", () -> Settings.passExplosionEventToBukkit && Settings.overrideMohistExplosionHandling && Utils.isClassLoaded("com.mohistmc.MohistMC"));
            put("mixins.bridge.permission.json", () -> Settings.bridgeForgePermissionsToBukkit);
        }
    });

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        if (!IS_HYBRID_ENV) return configs;
        if (!isClient) configs.addAll(serversideMixinConfigs.keySet());
        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if (!IS_HYBRID_ENV) return false;
        Supplier<Boolean> sidedSupplier = isClient ? null : serversideMixinConfigs.get(mixinConfig);
        if (sidedSupplier != null) {
            return sidedSupplier.get();
        }
        return true;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
