package network.ycc.raknet.server.channel;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.util.concurrent.PromiseCombiner;
import network.ycc.raknet.RakNet;

import java.net.SocketAddress;

public class RakNetApplicationChannel extends AbstractChannel {

    public static final String NAME_SERVER_PARENT_THREADED_READ_HANDLER = "rn-server-parent-threaded-read-handler";

    private volatile boolean active = false;

    protected RakNetApplicationChannel(RakNetChildChannel parent) {
        super(parent);
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
    public RakNetChildChannel parent() {
        return (RakNetChildChannel) super.parent();
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new RakNetApplicationChannelUnsafe();
    }

    @Override
    public Unsafe unsafe() {
        return ((RakNetApplicationChannelUnsafe) super.unsafe()).wrapped;
    }

    protected boolean isCompatible(EventLoop eventloop) {
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

    void setActiveInternal(boolean active) {
        this.active = active;
    }

    public ChannelMetadata metadata() {
        return parent().metadata();
    }

    @Override
    public ChannelFuture close() {
        if (this.isRegistered()) {
            return super.close();
        } else {
            return parent().close();
        }
    }

    protected final class RakNetApplicationChannelUnsafe extends AbstractUnsafe {

        // intercept calls
        final Unsafe wrapped = new Unsafe() {
            @SuppressWarnings("deprecation")
            @Override
            public RecvByteBufAllocator.Handle recvBufAllocHandle() {
                return RakNetApplicationChannelUnsafe.this.recvBufAllocHandle();
            }

            @Override
            public SocketAddress localAddress() {
                return RakNetApplicationChannelUnsafe.this.localAddress();
            }

            @Override
            public SocketAddress remoteAddress() {
                return RakNetApplicationChannelUnsafe.this.remoteAddress();
            }

            @Override
            public void register(EventLoop eventLoop, ChannelPromise promise) {
                RakNetApplicationChannelUnsafe.this.register(eventLoop, promise);
            }

            @Override
            public void bind(SocketAddress localAddress, ChannelPromise promise) {
                RakNetApplicationChannelUnsafe.this.bind(localAddress, promise);
            }

            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                RakNetApplicationChannelUnsafe.this.connect(remoteAddress, localAddress, promise);
            }

            @Override
            public void disconnect(ChannelPromise promise) {
                RakNetApplicationChannelUnsafe.this.disconnect(promise);
            }

            @Override
            public void close(ChannelPromise promise) {
                EventLoop parentEventLoop = parent().eventLoop(); // to prevent deregister race
                parent().close().addListener(future -> {
                    PromiseCombiner combiner = new PromiseCombiner();
                    ChannelPromise newPromise = promise.channel().newPromise();
                    parentEventLoop.execute(() -> { // wait for parent to finish deregistering
                        promise.channel().eventLoop().execute(() -> {
                            RakNetApplicationChannelUnsafe.this.close(newPromise);
                        });
                    });
                    combiner.add((ChannelFuture) newPromise);
                    combiner.add(future);
                    combiner.finish(promise);
                });
            }

            @Override
            public void closeForcibly() {
                RakNetApplicationChannelUnsafe.this.closeForcibly();
            }

            @Override
            public void deregister(ChannelPromise promise) {
                RakNetApplicationChannelUnsafe.this.deregister(promise);
            }

            @Override
            public void beginRead() {
                RakNetApplicationChannelUnsafe.this.beginRead();
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
                return RakNetApplicationChannelUnsafe.this.voidPromise();
            }

            @Override
            public ChannelOutboundBuffer outboundBuffer() {
                return RakNetApplicationChannelUnsafe.this.outboundBuffer();
            }
        };

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }
    }

}
