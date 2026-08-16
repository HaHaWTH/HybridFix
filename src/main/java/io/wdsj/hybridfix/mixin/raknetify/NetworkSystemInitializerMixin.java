package io.wdsj.hybridfix.mixin.raknetify;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.NetworkSystem$4")
public abstract class NetworkSystemInitializerMixin extends ChannelInitializer<Channel> {
    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"), remap = false)
    private void raknetify$init(Channel channel, CallbackInfo ci) {
        RaknetifyConnectionUtil112.initChannel(channel);
    }

    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("RETURN"), remap = false)
    private void raknetify$postInit(Channel channel, CallbackInfo ci) {
        RaknetifyConnectionUtil112.postInitChannel(channel);
    }
}
