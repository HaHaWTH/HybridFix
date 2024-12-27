package io.wdsj.hybridfix.mixin.bridge.explosion;

import io.wdsj.hybridfix.duck.bridge.explosion.IEntityGetter;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityGetter { // Some tricks to bypass Mixin invoker checks
}
