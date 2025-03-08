package io.wdsj.hybridfix.mixin.base.duck;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityGetter { // Method impl is provided by hybrids
}
