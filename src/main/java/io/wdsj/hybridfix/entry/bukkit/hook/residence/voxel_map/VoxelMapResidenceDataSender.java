package io.wdsj.hybridfix.entry.bukkit.hook.residence.voxel_map;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceSizeChangeEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.util.DataSender;
import io.wdsj.hybridfix.handler.voxelmap.SerializedResidence;
import io.wdsj.hybridfix.handler.voxelmap.VMResidenceChannel;
import io.wdsj.hybridfix.util.TickThread;
import io.wdsj.hybridfix.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("deprecation")
public class VoxelMapResidenceDataSender extends DataSender implements Listener {
    private static final int MAX_PACKET_SIZE = 30000;

    /**
     * Smaller value = higher priority.
     */
    private static final int PRIORITY_WORLD_CHANGE_FULL = 0;
    private static final int PRIORITY_JOIN_FULL = 10;
    private static final int PRIORITY_SINGLE_UPDATE = 50;

    private final PriorityBlockingQueue<QueuedSendTask> sendQueue = new PriorityBlockingQueue<>();

    private final AtomicLong sequence = new AtomicLong();

    private final AtomicBoolean workerStarted = new AtomicBoolean(false);

    private final ExecutorService senderExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY - 1)
                    .setNameFormat("HybridFix VoxelMap Data Sender-%d")
                    .build()
    );

    public VoxelMapResidenceDataSender() {
        startWorker();
    }

    private void startWorker() {
        if (!workerStarted.compareAndSet(false, true)) return;

        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    QueuedSendTask task = sendQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable t) {
                    HybridFix.LOGGER.error("[HybridFix] VoxelMap sender queue task failed", t);
                }
            }
        }, senderExecutor);
    }

    @EventHandler
    public void onDisableEvent(PluginDisableEvent event) {
        if (event.getPlugin() != HybridFixInternalPlugin.getInstance()) return;
        sendQueue.clear();
        senderExecutor.shutdownNow();
        workerStarted.set(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        enqueueFullSync(event.getPlayer(), 40L, PRIORITY_JOIN_FULL);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        enqueueFullSync(event.getPlayer(), 1L, PRIORITY_WORLD_CHANGE_FULL);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSizeChange(ResidenceSizeChangeEvent event) {
        if (event.getResidence() == null) return;

        ClaimedResidence res = event.getResidence();
        SerializedResidence newArea = toSerializedResidence(res.getName(), res.getOwner(), event.getNewArea());

        enqueueSingleUpdate(res.getWorld(), newArea, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceCreate(ResidenceCreationEvent event) {
        if (event.getResidence() == null) return;

        ClaimedResidence res = event.getResidence();
        SerializedResidence serializedResidence = toSerializedResidence(res);

        enqueueSingleUpdate(res.getWorld(), serializedResidence, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        if (event.getResidence() == null) return;

        ClaimedResidence res = event.getResidence();
        SerializedResidence serializedResidence = toSerializedResidence(res);

        enqueueSingleUpdate(res.getWorld(), serializedResidence, true);
    }

    private void enqueueFullSync(Player player, long delayTicks, int priority) {
        UUID playerId = player.getUniqueId();

        Bukkit.getScheduler().runTaskLater(HybridFixInternalPlugin.getInstance(), () -> {
            Player current = Bukkit.getPlayer(playerId);
            if (current == null || !current.isOnline()) return;
            String targetWorld = current.getWorld().getName();

            sendQueue.offer(new QueuedSendTask(
                    priority,
                    sequence.incrementAndGet(),
                    () -> runFullSync(playerId, targetWorld)
            ));
        }, delayTicks);
    }

    private void runFullSync(UUID playerId, String targetWorld) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        List<SerializedResidence> snapshot = snapshotWorldResidences(targetWorld);
                        if (snapshot == null) return null;

                        return buildWorldDataPackets(snapshot);
                    } catch (Exception e) {
                        HybridFix.LOGGER.error("[HybridFix] Failed to build residence packet", e);
                        return null;
                    }
                }, Utils.commonWorker())
                .thenAcceptAsync(packets -> {
                    if (packets == null) return;

                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) return;
                    if (!player.getWorld().getName().equals(targetWorld)) return;

                    clearResidences(player);

                    for (byte[] packet : packets) {
                        sendPluginMessage(player, VMResidenceChannel.CHANNEL, packet);
                    }
                }, TickThread.mainThreadExecutor())
                .join();
    }

    private void enqueueSingleUpdate(String worldName, SerializedResidence res, boolean isDelete) {
        sendQueue.offer(new QueuedSendTask(
                PRIORITY_SINGLE_UPDATE,
                sequence.incrementAndGet(),
                () -> runSingleUpdate(worldName, res, isDelete)
        ));
    }

    private void runSingleUpdate(String worldName, SerializedResidence res, boolean isDelete) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return isDelete ? buildSingleDataDeletePacket(res) : buildSingleDataPacket(res);
                    } catch (Exception e) {
                        HybridFix.LOGGER.error("[HybridFix] Failed to build single residence packet", e);
                        return null;
                    }
                }, Utils.commonWorker())
                .thenAcceptAsync(data -> {
                    if (data == null) return;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getWorld().getName().equals(worldName)) {
                            sendPluginMessage(p, VMResidenceChannel.CHANNEL, data);
                        }
                    }
                }, TickThread.mainThreadExecutor())
                .join();
    }

    private List<SerializedResidence> snapshotWorldResidences(String worldName) {
        ResidenceManager manager = Residence.getInstance().getResidenceManager();
        if (manager == null) return null;

        Collection<ClaimedResidence> allResidences = manager.getResidences().values();

        List<SerializedResidence> snapshot = new ArrayList<>();

        for (ClaimedResidence res : allResidences) {
            if (res.getMainArea() == null || res.getWorld() == null) continue;
            if (!res.getWorld().equals(worldName)) continue;

            snapshot.add(toSerializedResidence(res));
        }

        return snapshot;
    }

    private List<byte[]> buildWorldDataPackets(List<SerializedResidence> residences) throws IOException {
        List<byte[]> packets = new ArrayList<>();
        List<SerializedResidence> currentBatch = new ArrayList<>();
        int currentBatchSize = 0;

        for (SerializedResidence serializedRes : residences) {
            int itemSize = estimateSize(serializedRes);

            if (currentBatchSize + itemSize > MAX_PACKET_SIZE && !currentBatch.isEmpty()) {
                packets.add(serializeResidences(currentBatch, VMResidenceChannel.BATCH_UPDATE));
                currentBatch.clear();
                currentBatchSize = 0;
            }

            currentBatch.add(serializedRes);
            currentBatchSize += itemSize;
        }

        if (!currentBatch.isEmpty()) {
            packets.add(serializeResidences(currentBatch, VMResidenceChannel.BATCH_UPDATE));
        }

        return packets;
    }

    private int estimateSize(SerializedResidence res) {
        int nameLen = 2 + (res.name != null ? res.name.length() * 3 : 0);
        int ownerLen = 2 + (res.owner != null ? res.owner.length() * 3 : 0);
        return nameLen + ownerLen + 24;
    }

    private byte[] buildSingleDataPacket(SerializedResidence res) throws IOException {
        List<SerializedResidence> list = new ArrayList<>();
        list.add(res);
        return serializeResidences(list, VMResidenceChannel.SINGLE_UPDATE);
    }

    private byte[] buildSingleDataDeletePacket(SerializedResidence res) throws IOException {
        List<SerializedResidence> list = new ArrayList<>();
        list.add(res);
        return serializeResidences(list, VMResidenceChannel.SINGLE_REMOVE);
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

    private void clearResidences(Player player) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF(VMResidenceChannel.CLEAR);
            sendPluginMessage(player, VMResidenceChannel.CHANNEL, b.toByteArray());
        } catch (Exception e) {
            HybridFix.LOGGER.error("[HybridFix] Failed to clear residences", e);
        }
    }

    public static SerializedResidence toSerializedResidence(ClaimedResidence res) {
        return new SerializedResidence(
                res.getName(),
                res.getOwner(),
                res.getMainArea().getLowLocation().getBlockX(),
                res.getMainArea().getLowLocation().getBlockY(),
                res.getMainArea().getLowLocation().getBlockZ(),
                res.getMainArea().getHighLocation().getBlockX(),
                res.getMainArea().getHighLocation().getBlockY(),
                res.getMainArea().getHighLocation().getBlockZ()
        );
    }

    public static SerializedResidence toSerializedResidence(String areaName, String owner, CuboidArea area) {
        return new SerializedResidence(
                areaName,
                owner,
                area.getLowLocation().getBlockX(),
                area.getLowLocation().getBlockY(),
                area.getLowLocation().getBlockZ(),
                area.getHighLocation().getBlockX(),
                area.getHighLocation().getBlockY(),
                area.getHighLocation().getBlockZ()
        );
    }

    private static final class QueuedSendTask implements Comparable<QueuedSendTask> {
        private final int priority;
        private final long sequence;
        private final Runnable action;

        private QueuedSendTask(int priority, long sequence, Runnable action) {
            this.priority = priority;
            this.sequence = sequence;
            this.action = action;
        }

        private void run() {
            action.run();
        }

        @Override
        public int compareTo(QueuedSendTask other) {
            int priorityCompare = Integer.compare(this.priority, other.priority);
            if (priorityCompare != 0) return priorityCompare;

            return Long.compare(this.sequence, other.sequence);
        }
    }
}