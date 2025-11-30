package io.wdsj.hybridfix.mixin.fix.activation_range;

import io.wdsj.hybridfix.HybridFixServer;
import io.wdsj.hybridfix.duck.fix.activation_range.EntityEARAccessor;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityEARAccessor {
    @Unique
    private final boolean hybridFix$shouldIgnoreEAR = HybridFixServer.modEntitiesWithoutInactiveTick.contains(this.getClass());

    /**
     * @author Creeam
     * @reason Redirect to Entity#onUpdate() as mod entities do not have an inactiveTick()
     */
    @Dynamic("Spigot EAR")
    @Overwrite(remap = false)
    public void inactiveTick() {
        if (hybridFix$shouldIgnoreEAR) {
            ((Entity) (Object) this).onUpdate();
        }
    }

    @Override
    public boolean hybridFix$isIgnoringEAR() {
        return hybridFix$shouldIgnoreEAR;
    }
}
