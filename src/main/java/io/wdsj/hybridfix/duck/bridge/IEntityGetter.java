package io.wdsj.hybridfix.duck.bridge;

import io.wdsj.hybridfix.HybridFixServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

public interface IEntityGetter {
    default CraftEntity getBukkitEntity() {
        AssertionError error = new AssertionError("Not implemented");
        HybridFixServer.createServerDump(error);
        throw error;
    }
}
