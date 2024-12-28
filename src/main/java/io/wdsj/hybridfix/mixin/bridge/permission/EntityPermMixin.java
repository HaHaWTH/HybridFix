package io.wdsj.hybridfix.mixin.bridge.permission;

import io.wdsj.hybridfix.duck.bridge.permission.IEntityPermissionGetter;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityPermMixin implements IEntityPermissionGetter {
}
