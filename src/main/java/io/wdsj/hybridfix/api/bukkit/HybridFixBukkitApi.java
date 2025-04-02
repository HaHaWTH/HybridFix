package io.wdsj.hybridfix.api.bukkit;

import io.wdsj.hybridfix.api.forge.HybridFixForgeApi;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class HybridFixBukkitApi {
    protected HybridFixBukkitApi() {
    }
    private static final HybridFixBukkitApi INSTANCE = new HybridFixBukkitApi();

    /**
     * Gets the instance of {@link HybridFixForgeApi}.
     */
    public static HybridFixBukkitApi getApi() {
        return INSTANCE;
    }

    public boolean isFakePlayer(Player player) {
        return ((CraftPlayer) player).getHandle() instanceof FakePlayer;
    }
}
