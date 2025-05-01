package io.wdsj.hybridfix.api.forge;

import io.wdsj.hybridfix.duck.patch.forge.IExplosionEventDetonate;
import net.minecraftforge.event.world.ExplosionEvent;

public class HybridFixForgeApi {
    protected HybridFixForgeApi() {
    }
    private static final HybridFixForgeApi INSTANCE = new HybridFixForgeApi();

    /**
     * Gets the instance of {@link HybridFixForgeApi}.
     */
    public static HybridFixForgeApi getApi() {
        return INSTANCE;
    }

    /**
     * Whether this event is vanilla or not.
     * <b>NOTE:</b> Mods must call {@link HybridFixForgeApi#setVanillaExplosionEventDetonate(ExplosionEvent.Detonate, boolean)} to set this value.
     */
    public boolean isVanillaExplosionEventDetonate(ExplosionEvent.Detonate event) {
        return ((IExplosionEventDetonate) event).hybridFix$isVanilla();
    }

    /**
     * @deprecated Use {@link HybridFixForgeApi#isVanillaExplosionEventDetonate(ExplosionEvent.Detonate)} instead.
     */
    @Deprecated
    public boolean isVanillaExplosionDetonateEvent(ExplosionEvent.Detonate event) {
        return isVanillaExplosionEventDetonate(event);
    }

    /**
     * Sets whether this event is vanilla or not.
     * @param vanilla Whether this event is vanilla or not.
     */
    public void setVanillaExplosionEventDetonate(ExplosionEvent.Detonate event, boolean vanilla) {
        ((IExplosionEventDetonate) event).hybridFix$setVanilla(vanilla);
    }

    /**
     * @deprecated Use {@link HybridFixForgeApi#setVanillaExplosionEventDetonate(ExplosionEvent.Detonate, boolean)} instead.
     */
    @Deprecated
    public void setVanillaExplosionDetonateEvent(ExplosionEvent.Detonate event, boolean vanilla) {
        setVanillaExplosionEventDetonate(event, vanilla);
    }
}
