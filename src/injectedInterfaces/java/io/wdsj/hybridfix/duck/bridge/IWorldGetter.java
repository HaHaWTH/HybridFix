package io.wdsj.hybridfix.duck.bridge;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public interface IWorldGetter {
    default CraftWorld getWorld() {
        throw new AssertionError("Not implemented");
    }
}
