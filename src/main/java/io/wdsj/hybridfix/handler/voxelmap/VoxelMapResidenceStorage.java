package io.wdsj.hybridfix.handler.voxelmap;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.collection.SingleUserAreaMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.*;

/**
 * A client-side storage and spatial tracker for Residence data, specifically designed
 * to integrate with VoxelMap.
 * <p>
 * This class handles the reception of residence data from the server, stores them efficiently
 * and performs 2D spatial culling based on the player's
 * view distance to ensure only visible residences are passed to the rendering pipeline.
 *
 * @see SingleUserAreaMap
 */
public final class VoxelMapResidenceStorage {
    public static final VoxelMapResidenceStorage INSTANCE = new VoxelMapResidenceStorage();

    private final Object2ObjectOpenHashMap<String, SerializedResidence> allResidences = new Object2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<List<SerializedResidence>> chunkGrid = new Long2ObjectOpenHashMap<>();
    private final Reference2IntLinkedOpenHashMap<SerializedResidence> activeResidences = new Reference2IntLinkedOpenHashMap<>();

    private final ResidenceTracker tracker;

    private VoxelMapResidenceStorage() {
        this.tracker = new ResidenceTracker(this);
    }

    private static class ResidenceTracker extends SingleUserAreaMap<VoxelMapResidenceStorage> {
        public ResidenceTracker(VoxelMapResidenceStorage storage) {
            super(storage);
        }

        @Override
        protected void addCallback(VoxelMapResidenceStorage storage, int cx, int cz) {
            List<SerializedResidence> list = storage.chunkGrid.get(chunkKey(cx, cz));
            if (list != null) {
                for (SerializedResidence res : list) {
                    storage.activeResidences.addTo(res, 1);
                }
            }
        }

        @Override
        protected void removeCallback(VoxelMapResidenceStorage storage, int cx, int cz) {
            List<SerializedResidence> list = storage.chunkGrid.get(chunkKey(cx, cz));
            if (list != null) {
                for (SerializedResidence res : list) {
                    int current = storage.activeResidences.getInt(res);
                    if (current <= 1) {
                        storage.activeResidences.removeInt(res);
                    } else {
                        storage.activeResidences.put(res, current - 1);
                    }
                }
            }
        }
    }

    private static long chunkKey(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

    /**
     * Updates the player's current position and view distance, triggering visibility
     * calculations for residences.
     *
     * @param x  The player's absolute X coordinate.
     * @param z  The player's absolute Z coordinate.
     * @param vd The current render distance in chunks.
     */
    public void updatePlayerPos(double x, double z, int vd) {
        int cx = MathHelper.floor(x) >> 4;
        int cz = MathHelper.floor(z) >> 4;
        if (this.tracker.getLastChunkX() == SingleUserAreaMap.NOT_SET) {
            this.tracker.add(cx, cz, vd);
        } else {
            this.tracker.update(cx, cz, vd);
        }
    }

    /**
     * Inserts or updates a residence in the storage. Recalculates its presence
     * in the chunk grid and immediately marks it as active if it falls within
     * the player's current view distance.
     *
     * @param name The name of the residence.
     * @param res  The serialized residence data.
     */
    public void put(String name, SerializedResidence res) {
        remove(name);
        allResidences.put(name, res);

        int inViewCount = 0;
        for (int cx = res.minX >> 4; cx <= res.maxX >> 4; cx++) {
            for (int cz = res.minZ >> 4; cz <= res.maxZ >> 4; cz++) {
                chunkGrid.computeIfAbsent(chunkKey(cx, cz), k -> new ReferenceArrayList<>()).add(res);
                if (isChunkInTracker(cx, cz)) inViewCount++;
            }
        }
        if (inViewCount > 0) activeResidences.put(res, inViewCount);
    }

    /**
     * Completely removes a residence from the storage, cleaning up its references
     * from the chunk grid and active render list.
     *
     * @param name The name of the residence to remove.
     */
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
        activeResidences.removeInt(res);
    }

    private boolean isChunkInTracker(int cx, int cz) {
        int lx = tracker.getLastChunkX();
        if (lx == SingleUserAreaMap.NOT_SET) return false;
        int lz = tracker.getLastChunkZ();
        int d = tracker.getLastDistance();
        return cx >= lx - d && cx <= lx + d && cz >= lz - d && cz <= lz + d;
    }

    /**
     * Gets a collection of residences that are currently within the player's view distance
     * and should be rendered on the minimap or full-screen map.
     *
     * @return A sorted set of active {@link SerializedResidence}.
     */
    public ReferenceSortedSet<SerializedResidence> getActiveResidences() {
        return activeResidences.keySet();
    }

    /**
     * Gets a collection of all residences currently stored in memory for this dimension.
     *
     * @return A collection of all known {@link SerializedResidence}
     */
    public ObjectCollection<SerializedResidence> getAllResidences() {
        return allResidences.values();
    }

    /**
     * Clears all stored data, resetting the client-side state.
     * This is typically called when switching dimensions, disconnecting, or
     * receiving a full state update from the server.
     */
    public void clear() {
        allResidences.clear();
        chunkGrid.clear();
        activeResidences.clear();
        tracker.remove();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            this.clear();
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (!event.getPacket().channel().equals(VMResidenceChannel.CHANNEL)) return;

        byte[] data = new byte[event.getPacket().payload().readableBytes()];
        event.getPacket().payload().readBytes(data);

        String parsedType;
        Map<String, SerializedResidence> parsedUpdates = new Object2ObjectLinkedOpenHashMap<>();
        List<String> parsedRemoves = new ObjectArrayList<>();

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            parsedType = in.readUTF();
            if (!VMResidenceChannel.CLEAR.equals(parsedType)) {
                int count = in.readInt();
                for (int i = 0; i < count; i++) {
                    String name = in.readUTF();
                    String owner = in.readUTF();
                    int minX = in.readInt();
                    int minY = in.readInt();
                    int minZ = in.readInt();
                    int maxX = in.readInt();
                    int maxY = in.readInt();
                    int maxZ = in.readInt();

                    switch (parsedType) {
                        case VMResidenceChannel.SINGLE_UPDATE:
                        case VMResidenceChannel.BATCH_UPDATE:
                            parsedUpdates.put(name, new SerializedResidence(name, owner, minX, minY, minZ, maxX, maxY, maxZ));
                            break;
                        case VMResidenceChannel.SINGLE_REMOVE:
                            parsedRemoves.add(name);
                            break;
                    }
                }
            }
        } catch (Throwable t) {
            HybridFix.LOGGER.error("Failed to parse residence packet", t);
            return;
        }

        Minecraft.getMinecraft().addScheduledTask(() -> {
            try {
                switch (parsedType) {
                    case VMResidenceChannel.CLEAR:
                        this.clear();
                        break;
                    case VMResidenceChannel.SINGLE_UPDATE:
                    case VMResidenceChannel.BATCH_UPDATE:
                        for (Map.Entry<String, SerializedResidence> entry : parsedUpdates.entrySet()) {
                            this.put(entry.getKey(), entry.getValue());
                        }
                        break;
                    case VMResidenceChannel.SINGLE_REMOVE:
                        for (String name : parsedRemoves) {
                            this.remove(name);
                        }
                        break;
                }
            } catch (Throwable t) {
                HybridFix.LOGGER.error("Failed to apply residence state", t);
            }
        });
    }
}