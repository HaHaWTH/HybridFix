package io.wdsj.hybridfix.api.bukkit;

import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import io.wdsj.hybridfix.util.SneakyThrow;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
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
        EntityPlayerMP serverPlayer = ((CraftPlayer) player).getHandle();
        return serverPlayer instanceof FakePlayer || serverPlayer instanceof HybridFixFakePlayer.HybridFixDummyPlayer;
    }

    /**
     * Serializes the data of a player to string.
     * Works by saving player data to disk and re-reading it.
     *
     * @param player The player to serialize.
     * @return The serialized data of the player.
     */
    @Blocking
    @NotNull
    @ApiStatus.Experimental
    public String serializePlayerData(@NotNull Player player) {
        EntityPlayerMP serverPlayer = ((CraftPlayer) player).getHandle();
        WorldServer serverLevel = serverPlayer.getServerWorld();
        MinecraftServer server = serverLevel.getMinecraftServer();
        Objects.requireNonNull(server, "Server is null");
        PlayerList playerList = server.getPlayerList();
        playerList.playerDataManager.writePlayerData(serverPlayer);
        return Objects.requireNonNull(playerList.playerDataManager.readPlayerData(serverPlayer)).toString();
    }

    /**
     * Deserializes and applies the data of a player from string.
     * Warning: This method will overwrite the player's data.
     *
     * @param player The player to deserialize.
     * @param data   The serialized data of the player.
     */
    @Blocking
    @NotNull
    @ApiStatus.Experimental
    public Object deserializePlayerDataAndApply(@NotNull Player player, @NotNull String data) {
        try {
            EntityPlayerMP serverPlayer = ((CraftPlayer) player).getHandle();
            WorldServer serverLevel = serverPlayer.getServerWorld();
            MinecraftServer server = serverLevel.getMinecraftServer();
            Objects.requireNonNull(server, "Server is null");
            PlayerList playerList = server.getPlayerList();
            NBTTagCompound nbt = JsonToNBT.getTagFromJson(data);
            serverPlayer.readFromNBT(nbt);
            playerList.playerDataManager.writePlayerData(serverPlayer);
            Object fullNbt = Objects.requireNonNull(playerList.playerDataManager.readPlayerData(serverPlayer));
            playerList.syncPlayerInventory(serverPlayer);
            return fullNbt;
        } catch (Exception e) {
            SneakyThrow.sneaky(e);
            throw new RuntimeException(e); // never reached
        }
    }

    @NotNull
    public Object deserializeNBT(@NotNull String data) {
        try {
            return JsonToNBT.getTagFromJson(data);
        } catch (Exception e) {
            SneakyThrow.sneaky(e);
            throw new RuntimeException(e); // never reached
        }
    }
}
