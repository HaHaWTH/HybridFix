package io.wdsj.hybridfix.mixin.fix.respawn;

import com.google.common.collect.ImmutableList;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraftforge.fml.common.network.FMLOutboundHandler$OutboundTarget$3", remap = false)
public abstract class FMLOutboundHandlerMixin {
    @Dynamic
    @Inject(
            method = "selectNetworks",
            at = @At("HEAD"),
            cancellable = true
    )
    public void selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet, CallbackInfoReturnable<List<NetworkDispatcher>> cir) {
        if (packet.getDispatcher() == null) cir.setReturnValue(ImmutableList.of());
    }
}
