package io.wdsj.hybridfix.entry.bukkit.hook.residence.voxel_map;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceSizeChangeEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.AbstractResidenceDataSender;
import io.wdsj.hybridfix.handler.voxelmap.SerializedResidence;
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
    public void onResidenceSizeChange(ResidenceSizeChangeEvent event) {
        if (event.getResidence() == null) return;
        ClaimedResidence res = event.getResidence();
        SerializedResidence oldArea = toSerializedResidence(res.getName(), res.getOwner(), event.getOldArea());
        broadcastSingleUpdate(res.getWorld(), oldArea, true);
        SerializedResidence newArea = toSerializedResidence(res.getName(), res.getOwner(), event.getNewArea());
        broadcastSingleUpdate(res.getWorld(), newArea, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceCreate(ResidenceCreationEvent event) {
        if (event.getResidence() == null) return;
        ClaimedResidence res = event.getResidence();
        SerializedResidence serializedResidence = toSerializedResidence(event.getResidence());
        broadcastSingleUpdate(res.getWorld(), serializedResidence, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        if (event.getResidence() == null) return;
        ClaimedResidence res = event.getResidence();
        SerializedResidence serializedResidence = toSerializedResidence(event.getResidence());
        broadcastSingleUpdate(res.getWorld(), serializedResidence, true);
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
                        }
                    }, TickThread.mainThreadExecutor());
        }, delayTicks);
    }

    private void broadcastSingleUpdate(String worldName, SerializedResidence res, boolean isDelete) {
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
        List<SerializedResidence> targetResidences = new ArrayList<>();

        for (ClaimedResidence res : allResidences) {
            if (res.getMainArea() == null || res.getWorld() == null) continue;
            if (res.getWorld().equals(worldName)) {
                targetResidences.add(toSerializedResidence(res));
            }
        }

        return serializeResidences(targetResidences, PACKET_ALL);
    }

    private byte[] buildSingleDataPacket(SerializedResidence res) throws IOException {
        List<SerializedResidence> list = new ArrayList<>();
        list.add(res);
        return serializeResidences(list, PACKET_UPDATE);
    }

    private byte[] buildSingleDataDeletePacket(SerializedResidence res) throws IOException {
        List<SerializedResidence> list = new ArrayList<>();
        list.add(res);
        return serializeResidences(list, PACKET_REMOVE);
    }

    private byte[] serializeResidences(List<SerializedResidence> residences, String packetType) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        out.writeUTF(packetType);
        out.writeInt(residences.size());

        for (SerializedResidence res : residences) {
            out.writeUTF(res.name);
            out.writeUTF(res.owner);

            out.writeInt(res.minX);
            out.writeInt(res.minY);
            out.writeInt(res.minZ);
            out.writeInt(res.maxX);
            out.writeInt(res.maxY);
            out.writeInt(res.maxZ);
        }

        return b.toByteArray();
    }
    
    public static SerializedResidence toSerializedResidence(ClaimedResidence res) {
        return new SerializedResidence(res.getName(), res.getOwner(), res.getMainArea().getLowLocation().getBlockX(), res.getMainArea().getLowLocation().getBlockY(), res.getMainArea().getLowLocation().getBlockZ(), res.getMainArea().getHighLocation().getBlockX(), res.getMainArea().getHighLocation().getBlockY(), res.getMainArea().getHighLocation().getBlockZ());
    }

    public static SerializedResidence toSerializedResidence(String areaName, String owner, CuboidArea area) {
        return new SerializedResidence(areaName, owner, area.getLowLocation().getBlockX(), area.getLowLocation().getBlockY(), area.getLowLocation().getBlockZ(), area.getHighLocation().getBlockX(), area.getHighLocation().getBlockY(), area.getHighLocation().getBlockZ());
    }
}