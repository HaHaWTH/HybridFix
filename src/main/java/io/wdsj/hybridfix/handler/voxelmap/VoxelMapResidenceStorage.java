package io.wdsj.hybridfix.handler.voxelmap;

import io.netty.buffer.ByteBufInputStream;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.hook.residence.AbstractResidenceDataSender;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoxelMapResidenceStorage {
    public static final VoxelMapResidenceStorage INSTANCE = new VoxelMapResidenceStorage();

    private VoxelMapResidenceStorage() {
    }

    public final Map<String, SerializedResidence> areas = new ConcurrentHashMap<>();

    private static final String PACKET_ALL = "ALL";
    private static final String PACKET_UPDATE = "UPDATE";
    private static final String PACKET_REMOVE = "REMOVE";

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            areas.clear();
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (!event.getPacket().channel().equals(AbstractResidenceDataSender.CHANNEL)) return;

        try (DataInputStream in = new DataInputStream(new ByteBufInputStream(event.getPacket().payload()))) {

            String type = in.readUTF();
            int count = in.readInt();

            if (PACKET_ALL.equals(type)) {
                areas.clear();
            }

            for (int i = 0; i < count; i++) {
                String name = in.readUTF();
                String owner = in.readUTF();
                int minX = in.readInt();
                int minZ = in.readInt();
                int maxX = in.readInt();
                int maxZ = in.readInt();

                if (PACKET_REMOVE.equals(type)) {
                    areas.remove(name);
                } else {
                    areas.put(name, new SerializedResidence(name, owner, minX, minZ, maxX, maxZ));
                }
            }
        } catch (IOException | RuntimeException e) {
            HybridFix.LOGGER.error("Failed to handle residence packet", e);
        }
    }
}