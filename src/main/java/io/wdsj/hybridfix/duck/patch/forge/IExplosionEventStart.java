package io.wdsj.hybridfix.duck.patch.forge;

import org.jetbrains.annotations.ApiStatus;

/**
 * Internal API, use {@link io.wdsj.hybridfix.api.forge.HybridFixForgeApi} instead.
 */
@ApiStatus.Internal
public interface IExplosionEventStart {
    default boolean hybridFix$isVanilla() {
        return true;
    }

    default void hybridFix$setVanilla(boolean vanilla) {
    }
}
