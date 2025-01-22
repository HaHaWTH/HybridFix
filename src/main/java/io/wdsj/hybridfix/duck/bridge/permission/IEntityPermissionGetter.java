package io.wdsj.hybridfix.duck.bridge.permission;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

public interface IEntityPermissionGetter {
    default CraftEntity getBukkitEntity() {
        throw new AssertionError("Not implemented");
    }
}
