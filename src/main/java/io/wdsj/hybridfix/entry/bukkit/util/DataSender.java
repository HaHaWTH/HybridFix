package io.wdsj.hybridfix.entry.bukkit.util;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public abstract class DataSender {
    public void sendPluginMessage(Player player, String channel, byte[] data) {
        EntityPlayerMP serverPlayer = ((CraftPlayer) player).getHandle();
        if (serverPlayer.connection != null) {
            SPacketCustomPayload packet = new SPacketCustomPayload(channel, new PacketBuffer(Unpooled.wrappedBuffer(data)));
            serverPlayer.connection.sendPacket(packet);
        }
    }
}
