package io.wdsj.hybridfix.duck.bridge;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

public interface IEntityGetter {
    default CraftEntity getBukkitEntity() {
        throw new AssertionError("Not implemented");
    }
}
