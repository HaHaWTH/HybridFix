package io.wdsj.hybridfix.mixin.bridge.explosion.mohist;

import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExplosionEvent.Start.class)
public abstract class ExplosionStartEventMixin extends ExplosionEvent {
    public ExplosionStartEventMixin(World world, Explosion explosion) {
        super(world, explosion);
    }


    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/plugin/PluginManager;callEvent(Lorg/bukkit/event/Event;)V"
            ),
            remap = false
    )
    public void swallowEvent(PluginManager instance, Event event) {
        // no-op
    }
}
