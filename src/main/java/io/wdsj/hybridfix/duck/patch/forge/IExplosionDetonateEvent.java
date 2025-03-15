package io.wdsj.hybridfix.duck.patch.forge;

public interface IExplosionDetonateEvent {
    default boolean hybridFix$isVanilla() {
        return true;
    }

    default void hybridFix$setVanilla(boolean vanilla) {
    }
}
