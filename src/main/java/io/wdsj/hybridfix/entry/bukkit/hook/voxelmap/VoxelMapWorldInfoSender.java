package io.wdsj.hybridfix.entry.bukkit.hook.voxelmap;

import io.netty.buffer.Unpooled;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.entry.bukkit.HybridFixInternalPlugin;
import io.wdsj.hybridfix.entry.bukkit.util.DataSender;
import io.wdsj.hybridfix.util.TickThread;
import io.wdsj.hybridfix.util.Utils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.CompletableFuture;

public class VoxelMapWorldInfoSender extends DataSender implements Listener {
    private static final String WORLD_INFO_CHANNEL = "world_info";
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        sendWorldInfo(event.getPlayer(), 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        sendWorldInfo(event.getPlayer(), 1L);
    }

    public void sendWorldInfo(Player player, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(HybridFixInternalPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;
            final String targetWorld = player.getWorld().getName();
            CompletableFuture.supplyAsync(() -> {
                        try {
                            return buildWorldDataPacket(targetWorld);
                        } catch (Exception e) {
                            HybridFix.LOGGER.error("[HybridFix] Failed to build world info packet", e);
                            return null;
                        }
                    }, Utils.commonWorker())
                    .thenAcceptAsync(data -> {
                        if (data != null && player.isOnline() && player.getWorld().getName().equals(targetWorld)) {
                            sendPluginMessage(player, WORLD_INFO_CHANNEL, data);
                        }
                    }, TickThread.mainThreadExecutor());
        }, delayTicks);
    }

    private byte[] buildWorldDataPacket(String worldName) {
        String finalName = Bukkit.getServer().getServerName() + "_" + worldName;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(0);
        ByteBufUtils.writeUTF8String(buffer, finalName);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }
}
