package io.wdsj.hybridfix.api.forge.event.bukkit_mixin;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.List;

/**
 * Fired when Bukkit mixin setup is ready to accept mixins.
 * <br>
 * Registering mixins after this event is fired will have no effect.
 * <br>
 * This event is not {@link Cancelable}.<br>
 * This event does not use {@link HasResult}.<br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 */
@SuppressWarnings("unused")
public class BukkitMixinSetupEvent extends Event {
    private final List<String> mixinConfigs;
    public BukkitMixinSetupEvent(List<String> mixinConfigs) {
        this.mixinConfigs = mixinConfigs;
    }

    /**
     * Get the immutable view of mixin configs to be added to the mixin transformer.
     * @return the mixin configs
     */
    public List<String> getMixinConfigs() {
        return Collections.unmodifiableList(mixinConfigs);
    }

    /**
     * Add a mixin config to be added to the mixin transformer.
     * @param mixinConfig the mixin config
     */
    public void addMixinConfig(String mixinConfig) {
        mixinConfigs.add(mixinConfig);
    }
}
