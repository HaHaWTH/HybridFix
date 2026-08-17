package io.wdsj.hybridfix.mixin.raknetify;

import com.ishland.raknetify.common.connection.RakNetSimpleMultiChannelCodec;
import io.netty.channel.Channel;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import network.ycc.raknet.RakNet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
            method = "initializeConnectionToPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/network/NetHandlerPlayServer;)V",
            at = @At("HEAD"), remap = false
    )
    private void raknetify$logJoin(
            NetworkManager networkManager, EntityPlayerMP player, NetHandlerPlayServer handler, CallbackInfo ci) {
        Channel channel = networkManager.channel();
        if (RaknetifyConnectionUtil112.isRakNet(channel)) {
            RakNet.Config config = (RakNet.Config) channel.config();
            System.out.println("Raknetify: " + player.getName() + " logged in via RakNet, mtu " + config.getMTU());
        }
    }

    @Inject(
            method = "initializeConnectionToPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/network/NetHandlerPlayServer;)V",
            at = @At("RETURN"), remap = false
    )
    private void raknetify$startMultichannel(
            NetworkManager networkManager, EntityPlayerMP player, NetHandlerPlayServer handler, CallbackInfo ci) {
        Channel channel = networkManager.channel();
        if (RaknetifyConnectionUtil112.isRakNet(channel)) {
            channel.write(RakNetSimpleMultiChannelCodec.SIGNAL_START_MULTICHANNEL);
        }
    }
}
