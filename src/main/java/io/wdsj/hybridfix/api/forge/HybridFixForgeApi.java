package io.wdsj.hybridfix.api.forge;

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
        return event.hybridFix$isVanilla();
    }

    /**
     * Sets whether this event is vanilla or not.
     * @param vanilla Whether this event is vanilla or not.
     */
    public void setVanillaExplosionEventDetonate(ExplosionEvent.Detonate event, boolean vanilla) {
        event.hybridFix$setVanilla(vanilla);
    }

    /**
     * Whether this event is vanilla or not.
     * <b>NOTE:</b> Mods must call {@link HybridFixForgeApi#setVanillaExplosionEventStart(ExplosionEvent.Start, boolean)} to set this value.
     */
    public boolean isVanillaExplosionEventStart(ExplosionEvent.Start event) {
        return event.hybridFix$isVanilla();
    }

    /**
     * Sets whether this event is vanilla or not.
     * @param vanilla Whether this event is vanilla or not.
     */
    public void setVanillaExplosionEventStart(ExplosionEvent.Start event, boolean vanilla) {
        event.hybridFix$setVanilla(vanilla);
    }
}
