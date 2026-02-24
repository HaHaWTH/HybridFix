package io.wdsj.hybridfix.entry.bukkit.hook.residence.voxel_map;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.AbstractResidenceDataSender;
import io.wdsj.hybridfix.util.TickThread;
import io.wdsj.hybridfix.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VoxelMapResidenceDataSender extends AbstractResidenceDataSender implements Listener {
    private static final String PACKET_ALL = "ALL";
    private static final String PACKET_UPDATE = "UPDATE";
    private static final String PACKET_REMOVE = "REMOVE";
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendWorldResidences(event.getPlayer(), 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        sendWorldResidences(event.getPlayer(), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceCreate(ResidenceCreationEvent event) {
        if (event.getResidence() == null) return;
        broadcastSingleUpdate(event.getResidence(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        if (event.getResidence() == null) return;
        broadcastSingleUpdate(event.getResidence(), true);
    }

    public void sendWorldResidences(Player player, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(HybridFixInternalPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;
            final String targetWorld = player.getWorld().getName();
            CompletableFuture.supplyAsync(() -> {
                        try {
                            return buildWorldDataPacket(targetWorld);
                        } catch (Exception e) {
                            HybridFix.LOGGER.error("[HybridFix] Failed to build residence packet", e);
                            return null;
                        }
                    }, Utils.commonWorker())
                    .thenAcceptAsync(data -> {
                        if (data != null && player.isOnline() && player.getWorld().getName().equals(targetWorld)) {
                            player.sendPluginMessage(HybridFixInternalPlugin.getInstance(), CHANNEL, data);
                            HybridFix.LOGGER.info("[HybridFix] Sent residence data to {}", player.getName());
                        }
                    }, TickThread.mainThreadExecutor());
        }, delayTicks);
    }

    private void broadcastSingleUpdate(ClaimedResidence res, boolean isDelete) {
        if (res.getWorld() == null) return;
        final String worldName = res.getWorld();

        CompletableFuture.supplyAsync(() -> {
            try {
                return isDelete ? buildSingleDataDeletePacket(res) : buildSingleDataPacket(res);
            } catch (Exception e) {
                return null;
            }
        }, Utils.commonWorker()).thenAcceptAsync(data -> {
            if (data == null) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().getName().equals(worldName)) {
                    p.sendPluginMessage(HybridFixInternalPlugin.getInstance(), CHANNEL, data);
                }
            }
        }, TickThread.mainThreadExecutor());
    }

    private byte[] buildWorldDataPacket(String worldName) throws IOException {
        ResidenceManager manager = Residence.getInstance().getResidenceManager();
        if (manager == null) return null;

        Collection<ClaimedResidence> allResidences = manager.getResidences().values();
        List<ClaimedResidence> targetResidences = new ArrayList<>();

        for (ClaimedResidence res : allResidences) {
            if (res.getMainArea() == null || res.getWorld() == null) continue;
            if (res.getWorld().equals(worldName)) {
                targetResidences.add(res);
            }
        }

        return serializeResidences(targetResidences, PACKET_ALL);
    }

    private byte[] buildSingleDataPacket(ClaimedResidence res) throws IOException {
        List<ClaimedResidence> list = new ArrayList<>();
        list.add(res);
        return serializeResidences(list, PACKET_UPDATE);
    }

    private byte[] buildSingleDataDeletePacket(ClaimedResidence res) throws IOException {
        List<ClaimedResidence> list = new ArrayList<>();
        list.add(res);
        return serializeResidences(list, PACKET_REMOVE);
    }

    private byte[] serializeResidences(List<ClaimedResidence> residences, String packetType) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        out.writeUTF(packetType);
        out.writeInt(residences.size());

        for (ClaimedResidence res : residences) {
            CuboidArea area = res.getMainArea();
            if (area == null) continue;

            out.writeUTF(res.getName());
            out.writeUTF(res.getOwner() == null ? "Unknown" : res.getOwner());

            out.writeInt(area.getLowLocation().getBlockX());
            out.writeInt(area.getLowLocation().getBlockZ());
            out.writeInt(area.getHighLocation().getBlockX());
            out.writeInt(area.getHighLocation().getBlockZ());
        }

        return b.toByteArray();
    }
}