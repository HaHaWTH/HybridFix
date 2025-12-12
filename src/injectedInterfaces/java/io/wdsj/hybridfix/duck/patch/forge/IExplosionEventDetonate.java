package io.wdsj.hybridfix.duck.patch.forge;

@SuppressWarnings("unused")
public interface IExplosionEventDetonate {
    default boolean hybridFix$isVanilla() {
        return true;
    }

    default void hybridFix$setVanilla(boolean vanilla) {
    }
}
