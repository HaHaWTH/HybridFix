package io.wdsj.hybridfix.api.bukkit;

import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

public class HybridFixBukkitApi {
    protected HybridFixBukkitApi() {
    }
    private static final HybridFixBukkitApi INSTANCE = new HybridFixBukkitApi();

    /**
     * Gets the instance of {@link HybridFixBukkitApi}.
     */
    public static HybridFixBukkitApi getApi() {
        return INSTANCE;
    }

    /**
     * Checks if the given player is a Forge fake player.
     *
     * @param player The player to check.
     * @return {@code true} if the player is a fake player instance, {@code false} otherwise.
     */
    public boolean isFakePlayer(Player player) {
        return ((CraftEntity) player).getHandle() instanceof FakePlayer;
    }
}
