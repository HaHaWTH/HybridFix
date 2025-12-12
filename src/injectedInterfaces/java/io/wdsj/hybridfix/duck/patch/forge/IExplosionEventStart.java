package io.wdsj.hybridfix.duck.patch.forge;

public interface IExplosionEventStart {
    default boolean hybridFix$isVanilla() {
        return true;
    }

    default void hybridFix$setVanilla(boolean vanilla) {
    }
}
