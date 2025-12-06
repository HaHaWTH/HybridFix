package io.wdsj.hybridfix.duck.fix.activation_range;

public interface IEntityEARAccessor {
    default boolean hybridFix$isIgnoringEAR() {
        return false;
    }
}
