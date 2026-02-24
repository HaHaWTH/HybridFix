package io.wdsj.hybridfix.handler.voxelmap;

import io.netty.buffer.ByteBufInputStream;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.AbstractResidenceDataSender;
import io.wdsj.hybridfix.util.SingleUserAreaMap;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class VoxelMapResidenceStorage {
    public static final VoxelMapResidenceStorage INSTANCE = new VoxelMapResidenceStorage();

    public final Map<String, SerializedResidence> allResidences = new ConcurrentHashMap<>();
    private final Map<Long, List<SerializedResidence>> chunkGrid = new ConcurrentHashMap<>();
    private final Map<SerializedResidence, Integer> activeResidences = new ConcurrentHashMap<>();

    private final ResidenceTracker tracker;

    private VoxelMapResidenceStorage() {
        this.tracker = new ResidenceTracker(this);
    }

    private static class ResidenceTracker extends SingleUserAreaMap<VoxelMapResidenceStorage> {
        public ResidenceTracker(VoxelMapResidenceStorage cache) { super(cache); }

        @Override
        protected void addCallback(VoxelMapResidenceStorage cache, int cx, int cz) {
            List<SerializedResidence> list = cache.chunkGrid.get(chunkKey(cx, cz));
            if (list != null) {
                for (SerializedResidence res : list) {
                    cache.activeResidences.merge(res, 1, Integer::sum);
                }
            }
        }

        @Override
        protected void removeCallback(VoxelMapResidenceStorage cache, int cx, int cz) {
            List<SerializedResidence> list = cache.chunkGrid.get(chunkKey(cx, cz));
            if (list != null) {
                for (SerializedResidence res : list) {
                    cache.activeResidences.computeIfPresent(res, (r, count) -> {
                        if (count <= 1) {
                            return null;
                        } else {
                            return count - 1;
                        }
                    });
                }
            }
        }
    }

    private static long chunkKey(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

    public void updatePlayerPos(double x, double z, int vd) {
        int cx = MathHelper.floor(x) >> 4;
        int cz = MathHelper.floor(z) >> 4;
        if (this.tracker.getLastChunkX() == SingleUserAreaMap.NOT_SET) {
            this.tracker.add(cx, cz, vd);
        } else {
            this.tracker.update(cx, cz, vd);
        }
    }

    public void put(String name, SerializedResidence res) {
        remove(name);
        allResidences.put(name, res);

        int inViewCount = 0;
        for (int cx = res.minX >> 4; cx <= res.maxX >> 4; cx++) {
            for (int cz = res.minZ >> 4; cz <= res.maxZ >> 4; cz++) {
                chunkGrid.computeIfAbsent(chunkKey(cx, cz), k -> new CopyOnWriteArrayList<>()).add(res);
                if (isChunkInTracker(cx, cz)) inViewCount++;
            }
        }
        if (inViewCount > 0) activeResidences.put(res, inViewCount);
    }

    public void remove(String name) {
        SerializedResidence res = allResidences.remove(name);
        if (res == null) return;

        for (int cx = res.minX >> 4; cx <= res.maxX >> 4; cx++) {
            for (int cz = res.minZ >> 4; cz <= res.maxZ >> 4; cz++) {
                long key = chunkKey(cx, cz);
                List<SerializedResidence> list = chunkGrid.get(key);
                if (list != null) {
                    list.remove(res);
                    if (list.isEmpty()) chunkGrid.remove(key);
                }
            }
        }
        activeResidences.remove(res);
    }

    private boolean isChunkInTracker(int cx, int cz) {
        int lx = tracker.getLastChunkX();
        if (lx == SingleUserAreaMap.NOT_SET) return false;
        int lz = tracker.getLastChunkZ();
        int d = tracker.getLastDistance();
        return cx >= lx - d && cx <= lx + d && cz >= lz - d && cz <= lz + d;
    }

    public Collection<SerializedResidence> getActiveResidences() {
        return activeResidences.keySet();
    }

    public void clear() {
        allResidences.clear();
        chunkGrid.clear();
        activeResidences.clear();
        tracker.remove();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) this.clear();
    }

    private static final String PACKET_ALL = "ALL";
    private static final String PACKET_REMOVE = "REMOVE";
    private static final String PACKET_UPDATE = "UPDATE";

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (!event.getPacket().channel().equals(AbstractResidenceDataSender.CHANNEL)) return;
        try (DataInputStream in = new DataInputStream(new ByteBufInputStream(event.getPacket().payload()))) {
            String type = in.readUTF();
            int count = in.readInt();
            if (PACKET_ALL.equals(type)) this.clear(); // this will be handled later by updatePlayerPos
            for (int i = 0; i < count; i++) {
                String name = in.readUTF();
                String owner = in.readUTF();
                int minX = in.readInt();
                int minZ = in.readInt();
                int maxX = in.readInt();
                int maxZ = in.readInt();
                if (PACKET_REMOVE.equals(type)) {
                    this.remove(name);
                } else {
                    this.put(name, new SerializedResidence(name, owner, minX, minZ, maxX, maxZ));
                }
            }
        } catch (IOException | RuntimeException e) {
            HybridFix.LOGGER.error("Failed to handle residence packet", e);
        }
    }
}