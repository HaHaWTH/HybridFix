package io.wdsj.hybridfix.mixin.base.patch.forge;

import io.wdsj.hybridfix.duck.patch.forge.IExplosionEventDetonate;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ExplosionEvent.Detonate.class)
public abstract class ExplosionEventDetonateMixin implements IExplosionEventDetonate {
    @Unique
    private boolean hybridFix$vanilla = true;

    @Override
    public boolean hybridFix$isVanilla() {
        return hybridFix$vanilla;
    }

    @Override
    public void hybridFix$setVanilla(boolean vanilla) {
        this.hybridFix$vanilla = vanilla;
    }
}
