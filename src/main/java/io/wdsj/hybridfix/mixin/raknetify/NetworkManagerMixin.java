package io.wdsj.hybridfix.mixin.raknetify;

import com.ishland.raknetify.common.Constants;
import com.ishland.raknetify.common.connection.MultiChannelingStreamingCompression;
import com.ishland.raknetify.common.connection.MultiChannellingEncryption;
import com.ishland.raknetify.common.util.DebugUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.SecretKey;
import java.nio.channels.ClosedChannelException;
import java.security.GeneralSecurityException;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin {
    @Shadow public Channel channel;
    @Shadow private boolean isEncrypted;
    @Unique private volatile boolean raknetify$isClosing;

    @Redirect(
            method = "closeChannel",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/channel/Channel;close()Lio/netty/channel/ChannelFuture;",
                    remap = false
            )
    )
    private ChannelFuture raknetify$markClosing(Channel instance) {
        ChannelFuture future = instance.close();
        this.raknetify$isClosing = true;
        return future;
    }

    @Redirect(
            method = "closeChannel",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/channel/ChannelFuture;awaitUninterruptibly()Lio/netty/channel/ChannelFuture;",
                    remap = false
            )
    )
    private ChannelFuture raknetify$doNotWaitForClose(ChannelFuture future) {
        return future;
    }

    @Redirect(
            method = {"channelRead0*", "flushOutboundQueue", "closeChannel", "isChannelOpen", "handleDisconnection"},
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/channel/Channel;isOpen()Z",
                    remap = false
            )
    )
    private boolean raknetify$treatClosingAsClosed(Channel instance) {
        return instance != null && instance.isOpen() && !this.raknetify$isClosing;
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void raknetify$diagnoseException(ChannelHandlerContext context, Throwable exception, CallbackInfo ci) {
        if (exception instanceof ClosedChannelException || this.channel == null) {
            return;
        }
        if (Constants.DEBUG) {
            HybridFix.LOGGER.warn("Exception caught for connection {}", this.channel);
            for (String line : DebugUtil.printChannelDetails(this.channel).split("\n")) {
                HybridFix.LOGGER.warn("  {}", line);
            }
            HybridFix.LOGGER.warn(exception);
            return;
        }

        EnumConnectionState state = this.channel.attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get();
        if (state != null && state != EnumConnectionState.HANDSHAKING) {
            HybridFix.LOGGER.warn("{} {} {}", this.channel.remoteAddress(), state, exception);
        }
    }

    @Inject(method = "enableEncryption", at = @At("HEAD"), cancellable = true)
    private void raknetify$enableFrameEncryption(SecretKey key, CallbackInfo ci) {
        if (!RaknetifyConnectionUtil112.isRakNet(this.channel)) {
            return;
        }
        try {
            this.isEncrypted = true;
            hybridFix$removeIfPresent("decrypt");
            hybridFix$removeIfPresent("encrypt");
            hybridFix$removeIfPresent(MultiChannellingEncryption.NAME);
            this.channel.pipeline().addBefore(
                    MultiChannelingStreamingCompression.NAME,
                    MultiChannellingEncryption.NAME,
                    new MultiChannellingEncryption(key));
            ci.cancel();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to initialize RakNet frame encryption", e);
        }
    }

    @Unique
    private void hybridFix$removeIfPresent(String name) {
        if (this.channel.pipeline().get(name) != null) {
            this.channel.pipeline().remove(name);
        }
    }
}
