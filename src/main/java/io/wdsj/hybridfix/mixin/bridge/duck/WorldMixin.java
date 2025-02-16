package io.wdsj.hybridfix.mixin.bridge.duck;

import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class WorldMixin implements IWorldGetter { // Method impl is provided by hybrids
}
