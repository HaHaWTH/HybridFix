package network.ycc.raknet.client.channel;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.concurrent.PromiseCombiner;
import network.ycc.raknet.RakNet;

import java.net.SocketAddress;
import java.util.function.Supplier;

public class RakNetClientThreadedChannel extends AbstractChannel {

    public static final String NAME_CLIENT_PARENT_THREADED_READ_HANDLER = "rn-client-parent-threaded-read-handler";

    private EventLoop providedParentEventLoop = null;
    private EventLoop pendingEventLoop = null;

    private volatile boolean active = false;

    public RakNetClientThreadedChannel() {
        super(new RakNetClientChannel());
        setupDefaultPipeline();
    }

    public RakNetClientThreadedChannel(Class<? extends DatagramChannel> ioChannelType) {
        super(new RakNetClientChannel(ioChannelType));
        setupDefaultPipeline();
    }

    public RakNetClientThreadedChannel(Supplier<? extends DatagramChannel> ioChannelSupplier) {
        super(new RakNetClientChannel(ioChannelSupplier));
        setupDefaultPipeline();
    }

    @Deprecated
    public void setProvidedEventLoop(EventLoop providedEventLoop) {
        this.setProvidedParentEventLoop(providedEventLoop);
    }

    /**
     * Set the event loop to be used for the IO thread.
     * Required if this channel is registered with an event loop incompatible with the IO used.
     *
     * @param providedParentEventLoop the event loop
     */
    public void setProvidedParentEventLoop(EventLoop providedParentEventLoop) {
        this.providedParentEventLoop = providedParentEventLoop;
    }

    private void setupDefaultPipeline() {
        parent().pipeline().addLast(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(NAME_CLIENT_PARENT_THREADED_READ_HANDLER, new ParentReadHandler());
            }
        });
    }

    @Override
    public boolean isWritable() {
        final Boolean result = attr(RakNet.WRITABLE).get();
        return (result == null || result) && parent().isWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return parent().bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return parent().bytesBeforeWritable();
    }

    @Override
    public RakNetClientChannel parent() {
        return (RakNetClientChannel) super.parent();
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new RakNetClientThreadedChannelUnsafe();
    }

    @Override
    public Unsafe unsafe() {
        return ((RakNetClientThreadedChannelUnsafe) super.unsafe()).wrapped;
    }

    protected boolean isCompatible(EventLoop eventloop) {
        this.pendingEventLoop = eventloop;
        return true;
    }

    protected SocketAddress localAddress0() {
        return parent().localAddress();
    }

    protected SocketAddress remoteAddress0() {
        return parent().remoteAddress();
    }

    protected void doBind(SocketAddress addr) {
        throw new UnsupportedOperationException();
    }

    protected void doDisconnect() {
//        close();
        this.active = false;
        parent().close();
    }

    protected void doClose() {
//        close();
        this.active = false;
        parent().close();
    }

    protected void doBeginRead() {
        // NOOP
    }

    protected void doWrite(ChannelOutboundBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    public RakNet.Config config() {
        return parent().config();
    }

    public boolean isOpen() {
        return parent().isOpen();
    }

    public boolean isActive() {
        return this.active;
    }

    public ChannelMetadata metadata() {
        return parent().metadata();
    }

    protected final class RakNetClientThreadedChannelUnsafe extends AbstractUnsafe {

        // intercept calls
        final Unsafe wrapped = new Unsafe() {
            @SuppressWarnings("deprecation")
            @Override
            public RecvByteBufAllocator.Handle recvBufAllocHandle() {
                return RakNetClientThreadedChannelUnsafe.this.recvBufAllocHandle();
            }

            @Override
            public SocketAddress localAddress() {
                return RakNetClientThreadedChannelUnsafe.this.localAddress();
            }

            @Override
            public SocketAddress remoteAddress() {
                return RakNetClientThreadedChannelUnsafe.this.remoteAddress();
            }

            @Override
            public void register(EventLoop eventLoop, ChannelPromise promise) {
                RakNetClientThreadedChannelUnsafe.this.register(eventLoop, promise);
            }

            @Override
            public void bind(SocketAddress localAddress, ChannelPromise promise) {
                registerParent();
                final ChannelFuture future = parent().bind(localAddress);
                future.addListener(RakNet.INTERNAL_WRITE_LISTENER);
                future.addListener(future1 -> {
                    if (future1.isSuccess()) promise.trySuccess();
                    else promise.tryFailure(future1.cause());
                });
            }

            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                RakNetClientThreadedChannelUnsafe.this.connect(remoteAddress, localAddress, promise);
            }

            @Override
            public void disconnect(ChannelPromise promise) {
                RakNetClientThreadedChannelUnsafe.this.disconnect(promise);
            }

            @Override
            public void close(ChannelPromise promise) {
                EventLoop parentEventLoop = parent().eventLoop(); // to prevent deregister race
                parent().close().addListener(future -> {
                    PromiseCombiner combiner = new PromiseCombiner();
                    ChannelPromise newPromise = promise.channel().newPromise();
                    parentEventLoop.execute(() -> { // wait for parent to finish deregistering
                        promise.channel().eventLoop().execute(() -> {
                            RakNetClientThreadedChannelUnsafe.this.close(newPromise);
                        });
                    });
                    combiner.add((ChannelFuture) newPromise);
                    combiner.add(future);
                    combiner.finish(promise);
                });
            }

            @Override
            public void closeForcibly() {
                RakNetClientThreadedChannelUnsafe.this.closeForcibly();
            }

            @Override
            public void deregister(ChannelPromise promise) {
                RakNetClientThreadedChannelUnsafe.this.deregister(promise);
            }

            @Override
            public void beginRead() {
                RakNetClientThreadedChannelUnsafe.this.beginRead();
            }

            @Override
            public void write(Object msg, ChannelPromise promise) {
                // we don't use ChannelOutboundBuffer
                final ChannelFuture future = parent().write(msg);
                future.addListener(RakNet.INTERNAL_WRITE_LISTENER);
                future.addListener(future1 -> {
                    if (future1.isSuccess()) promise.trySuccess();
                    else promise.tryFailure(future1.cause());
                });
            }

            @Override
            public void flush() {
                parent().flush();
            }

            @Override
            public ChannelPromise voidPromise() {
                return RakNetClientThreadedChannelUnsafe.this.voidPromise();
            }

            @Override
            public ChannelOutboundBuffer outboundBuffer() {
                return RakNetClientThreadedChannelUnsafe.this.outboundBuffer();
            }
        };

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            registerParent();
            final ChannelFuture future = parent().connect(remoteAddress, localAddress);
            future.addListener(RakNet.INTERNAL_WRITE_LISTENER);
            future.addListener(future1 -> {
                if (future1.isSuccess()) promise.trySuccess();
                else promise.tryFailure(future1.cause());
            });
        }
    }

    private void registerParent() {
        if (!parent().isRegistered()) {
            EventLoop loop = this.providedParentEventLoop != null ? this.providedParentEventLoop : this.pendingEventLoop;
            if (loop == null)
                throw new IllegalStateException("channel not registered");
            loop.register(parent());
        }
    }

    protected class ParentReadHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            RakNetClientThreadedChannel.this.eventLoop().execute(() -> {
                RakNetClientThreadedChannel.this.active = true;
                RakNetClientThreadedChannel.this.pipeline().fireChannelActive();
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            RakNetClientThreadedChannel.this.eventLoop().execute(() -> {
                RakNetClientThreadedChannel.this.active = false;
                RakNetClientThreadedChannel.this.pipeline().fireChannelInactive();
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            RakNetClientThreadedChannel.this.pipeline().fireChannelRead(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            RakNetClientThreadedChannel.this.pipeline().fireChannelReadComplete();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            RakNetClientThreadedChannel.this.pipeline().fireUserEventTriggered(evt);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) {
            RakNetClientThreadedChannel.this.pipeline().fireChannelWritabilityChanged();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            RakNetClientThreadedChannel.this.pipeline().fireExceptionCaught(cause);
        }

    }

}
