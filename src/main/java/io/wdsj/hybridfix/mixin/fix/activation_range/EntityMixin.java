package io.wdsj.hybridfix.mixin.fix.activation_range;

import io.wdsj.hybridfix.HybridFixServer;
import io.wdsj.hybridfix.duck.fix.activation_range.IEntityEARAccessor;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("unchecked")
@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityEARAccessor {
    @Unique
    private final boolean hybridFix$shouldIgnoreEAR = HybridFixServer.checkIfIgnoreEAR((Class<? extends Entity>) (Object) this.getClass());
    @Override
    public boolean hybridFix$isIgnoringEAR() {
        return hybridFix$shouldIgnoreEAR;
    }
}
