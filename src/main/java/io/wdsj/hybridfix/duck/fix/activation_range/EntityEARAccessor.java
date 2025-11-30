package io.wdsj.hybridfix.duck.fix.activation_range;

public interface EntityEARAccessor {
    default boolean hybridFix$isIgnoringEAR() {
        return false;
    }
}
