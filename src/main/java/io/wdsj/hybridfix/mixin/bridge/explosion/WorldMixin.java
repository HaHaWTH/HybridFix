package io.wdsj.hybridfix.mixin.bridge.explosion;

import io.wdsj.hybridfix.duck.bridge.explosion.IWorldGetter;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class WorldMixin implements IWorldGetter {
}
