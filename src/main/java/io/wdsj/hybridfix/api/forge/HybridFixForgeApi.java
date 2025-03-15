package io.wdsj.hybridfix.api.forge;

import io.wdsj.hybridfix.duck.patch.forge.IExplosionDetonateEvent;
import net.minecraftforge.event.world.ExplosionEvent;

@SuppressWarnings("unused")
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
     * <b>NOTE:</b> Mods must call {@link HybridFixForgeApi#setVanillaExplosionDetonateEvent(ExplosionEvent.Detonate, boolean)} to set this value.
     */
    public boolean isVanillaExplosionDetonateEvent(ExplosionEvent.Detonate event) {
        return ((IExplosionDetonateEvent) event).hybridFix$isVanilla();
    }

    /**
     * Sets whether this event is vanilla or not.
     * @param vanilla Whether this event is vanilla or not.
     */
    public void setVanillaExplosionDetonateEvent(ExplosionEvent.Detonate event, boolean vanilla) {
        ((IExplosionDetonateEvent) event).hybridFix$setVanilla(vanilla);
    }
}
