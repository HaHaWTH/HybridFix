package network.ycc.raknet.server.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.wdsj.hybridfix.HybridFix;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.config.DefaultConfig;
import network.ycc.raknet.pipeline.FlushTickHandler;
import network.ycc.raknet.server.RakNetServer;
import network.ycc.raknet.server.pipeline.ConnectionInitializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RakNetChildChannel extends AbstractChannel {

    public static final String WRITE_HANDLER_NAME = "rn-child-channel-write-handler";

    private static final ChannelMetadata metadata = new ChannelMetadata(false);
    protected final ChannelPromise connectPromise;
    protected final ChannelPromise closePromise;
    protected final RakNet.Config config;
    protected final InetSocketAddress remoteAddress;
    protected final InetSocketAddress localAddress;
    protected final RakNetApplicationChannel applicationChannel;
    protected final AtomicBoolean isRegisteringApplicationChannel = new AtomicBoolean(false);
    protected final Queue<Runnable> pendingApplicationChannelOperations = new LinkedList<>();
    protected final Consumer<Channel> registerChannel;
    private final DatagramChannel listener;

    protected volatile boolean open = true;

    public RakNetChildChannel(Supplier<? extends DatagramChannel> ioChannelSupplier, Channel parent, InetSocketAddress remoteAddress, InetSocketAddress localAddress, Consumer<Channel> registerChannel) {
        super(parent);
        this.listener = ioChannelSupplier.get();
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.registerChannel = Objects.requireNonNull(registerChannel);
        config = new DefaultConfig(this);
        connectPromise = newPromise();
        closePromise = newPromise();
        config.setMetrics(parent.config().getOption(RakNet.METRICS));
        config.setServerId(parent.config().getOption(RakNet.SERVER_ID));
        this.applicationChannel = new RakNetApplicationChannel(this);
        pipeline().addLast(WRITE_HANDLER_NAME, new WriteHandler());
        addDefaultPipeline();
    }

    protected void addDefaultPipeline() {
        pipeline().addLast(RakNetServer.DefaultChildInitializer.INSTANCE);
        connectPromise.addListener(x2 -> {
            if (!x2.isSuccess()) {
                RakNetChildChannel.this.applicationChannel.close();
                RakNetChildChannel.this.close();
            }
        });
        pipeline().addLast(new ChannelInitializer<RakNetChildChannel>() {
            protected void initChannel(RakNetChildChannel ch) {
                pipeline().replace(ConnectionInitializer.NAME, ConnectionInitializer.NAME,
                        new ConnectionInitializer(connectPromise));
                pipeline().addLast(RakNetApplicationChannel.NAME_SERVER_PARENT_THREADED_READ_HANDLER, new ReadHandler());
            }
        });
    }

    protected void registerApplicationChannelIfNecessary() {
        if (!this.applicationChannel.isRegistered()) {
            if (isRegisteringApplicationChannel.compareAndSet(false, true)) {
                registerChannel.accept(this.applicationChannel);
            }
        } else if (!pendingApplicationChannelOperations.isEmpty()) {
            Runnable r;
            while ((r = pendingApplicationChannelOperations.poll()) != null) {
                try {
                    r.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public ChannelFuture connectFuture() {
        return connectPromise;
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
    public RakNetServerChannel parent() {
        return (RakNetServerChannel) super.parent();
    }

    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            public void connect(SocketAddress addr1, SocketAddress addr2, ChannelPromise pr) {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected boolean isCompatible(EventLoop eventloop) {
        return true;
    }

    protected SocketAddress localAddress0() {
        return localAddress;
    }

    protected SocketAddress remoteAddress0() {
        return remoteAddress;
    }

    protected void doBind(SocketAddress addr) {
        throw new UnsupportedOperationException();
    }

    protected void doDisconnect() {
        close();
    }

    protected void doClose() {
        open = false;
        if (this.applicationChannel.isRegistered()) {
            this.applicationChannel.close();
        }
    }

    protected void doBeginRead() {
        // NOOP
    }

    protected void doWrite(ChannelOutboundBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    public RakNet.Config config() {
        return config;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isActive() {
        return isOpen() && parent().isActive() && connectPromise.isSuccess();
    }

    public ChannelMetadata metadata() {
        return metadata;
    }

    public RakNetApplicationChannel getApplicationChannel() {
        return this.applicationChannel;
    }

    protected synchronized ChannelPromise initStandaloneListener() {
        ChannelPromise channelPromise = this.newPromise();
        if (listener.isRegistered()) {
            HybridFix.LOGGER.info("Already registered?");
            channelPromise.trySuccess();
            return channelPromise;
        }
        ChannelUtil.setChannelOptions(listener, parent().getChannelParameters().childOptions);
        listener.config()
                .setReuseAddress(true)
                .setAutoRead(true)
                .setRecvByteBufAllocator(this.parent().backingChannel().config().getRecvByteBufAllocator());
        listener.pipeline()
                .addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) {
                        ctx.fireChannelActive();
                        ctx.read();
                        RakNetChildChannel.this.pipeline().replace(WRITE_HANDLER_NAME, WRITE_HANDLER_NAME, new ListenerOutboundProxy());
                        ctx.pipeline().remove(this);
                    }
                })
                .addLast(WRITE_HANDLER_NAME, new ListenerInboundProxy())
                .addLast(new FlushConsolidationHandler(256, true));
        this.eventLoop().register(this.listener).addListener(future -> {
            if (future.isSuccess()) {
                listener.connect(this.remoteAddress, this.localAddress).addListener(future1 -> {
                    if (future1.isDone()) {
                        channelPromise.trySuccess();
                    } else {
                        channelPromise.setFailure(future1.cause());
                        future1.cause().printStackTrace();
                    }
                });
            } else {
                channelPromise.setFailure(future.cause());
                future.cause().printStackTrace();
            }
        });
        return channelPromise;
    }

    protected ChannelPromise wrapPromise(ChannelPromise in) {
        final ChannelPromise out = listener.newPromise();
        out.addListener(res -> {
            if (res.isSuccess()) {
                in.trySuccess();
            } else {
                in.tryFailure(res.cause());
            }
        });
        return out;
    }

    protected class WriteHandler extends ChannelOutboundHandlerAdapter {
        protected boolean needsFlush = false;

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            if (msg instanceof ByteBuf) {
                needsFlush = true;
                promise.trySuccess();
                parent().write(new DatagramPacket((ByteBuf) msg, remoteAddress, localAddress))
                        .addListener(RakNet.INTERNAL_WRITE_LISTENER);
            } else {
                ctx.write(msg, promise);
            }
        }

        @Override
        public void flush(ChannelHandlerContext ctx) {
            if (needsFlush) {
                needsFlush = false;
                parent().flush();
            }
        }

        @Override
        public void read(ChannelHandlerContext ctx) {
            // NOOP
        }
    }

    protected class ReadHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            registerApplicationChannelIfNecessary();
            initStandaloneListener().addListener(future -> {
                if (applicationChannel.isRegistered()) {
                    channelActive0();
                } else {
                    pendingApplicationChannelOperations.add(this::channelActive0);
                }
            });
        }

        private void channelActive0() {
            applicationChannel.eventLoop().execute(() -> {
                applicationChannel.setActiveInternal(true);
                applicationChannel.pipeline().fireChannelActive();
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            registerApplicationChannelIfNecessary();
            if (applicationChannel.isRegistered()) {
                channelInactive0();
            } else {
                pendingApplicationChannelOperations.add(this::channelInactive0);
            }
        }

        private void channelInactive0() {
            applicationChannel.eventLoop().execute(() -> {
                applicationChannel.setActiveInternal(false);
                applicationChannel.pipeline().fireChannelInactive();
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            registerApplicationChannelIfNecessary();
            if (applicationChannel.isRegistered()) {
                applicationChannel.pipeline().fireChannelRead(msg);
            } else {
                pendingApplicationChannelOperations.add(() -> applicationChannel.pipeline().fireChannelRead(msg));
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            if (!applicationChannel.isRegistered()) return;
            applicationChannel.pipeline().fireChannelReadComplete();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (applicationChannel.isRegistered()) {
                registerApplicationChannelIfNecessary();
            }
            if (evt == FlushTickHandler.FLUSH_CHECK_SIGNAL || evt instanceof FlushTickHandler.MissedFlushes) {
                return;
            }
            registerApplicationChannelIfNecessary();
            if (applicationChannel.isRegistered()) {
                applicationChannel.pipeline().fireUserEventTriggered(evt);
            } else {
                pendingApplicationChannelOperations.add(() -> applicationChannel.pipeline().fireUserEventTriggered(evt));
            }
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) {
            registerApplicationChannelIfNecessary();
            if (applicationChannel.isRegistered()) {
                applicationChannel.pipeline().fireChannelWritabilityChanged();
            } else {
                pendingApplicationChannelOperations.add(() -> applicationChannel.pipeline().fireChannelWritabilityChanged());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            registerApplicationChannelIfNecessary();
            if (applicationChannel.isRegistered()) {
                applicationChannel.pipeline().fireExceptionCaught(cause);
            } else {
                pendingApplicationChannelOperations.add(() -> applicationChannel.pipeline().fireExceptionCaught(cause));
            }
        }
    }

    protected class ListenerOutboundProxy implements ChannelOutboundHandler {

        public void handlerAdded(ChannelHandlerContext ctx) {
            assert listener.eventLoop().inEventLoop();
        }

        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
                         ChannelPromise promise) {
//            listener.bind(localAddress, wrapPromise(promise));
            promise.setFailure(new UnsupportedOperationException());
        }

        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                            SocketAddress localAddress, ChannelPromise promise) {
//            listener.connect(remoteAddress, localAddress, wrapPromise(promise));
            promise.setFailure(new UnsupportedOperationException());
        }

        public void handlerRemoved(ChannelHandlerContext ctx) {
            // NOOP
        }

        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
//            listener.disconnect(wrapPromise(promise));
            promise.setFailure(new UnsupportedOperationException());
        }

        public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (listener.isRegistered()) {
                ChannelPromise listenerClose = ctx.newPromise();
                listener.close(wrapPromise(listenerClose));
                listenerClose.addListener(future -> {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                    }
                    ctx.close(promise);
                });
            } else {
                ctx.close(promise);
            }
        }

        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            ctx.deregister(promise);
        }

        public void read(ChannelHandlerContext ctx) {
            // NOOP
        }

        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            listener.write(msg, wrapPromise(promise)).addListener(RakNet.INTERNAL_WRITE_LISTENER);
        }

        public void flush(ChannelHandlerContext ctx) {
            listener.flush();
        }

        @SuppressWarnings("deprecation")
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof NoRouteToHostException) {
                return;
            }
            ctx.fireExceptionCaught(cause);
        }

    }

    protected class ListenerInboundProxy implements ChannelInboundHandler {

        public void channelRegistered(ChannelHandlerContext ctx) {
        }

        public void channelUnregistered(ChannelHandlerContext ctx) {
        }

        public void handlerAdded(ChannelHandlerContext ctx) {
            assert listener.eventLoop().inEventLoop();
        }

        public void channelActive(ChannelHandlerContext ctx) {
            // NOOP - active status managed by connection sequence
        }

        public void channelInactive(ChannelHandlerContext ctx) {
        }

        public void handlerRemoved(ChannelHandlerContext ctx) {
            // NOOP
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof DatagramPacket) {
                DatagramPacket datagram = (DatagramPacket) msg;
                try {
                    if (isActive()) {
                        final ByteBuf retained = datagram.content().retain();
                        pipeline().fireChannelRead(retained);
                    }
                } finally {
                    datagram.release();
                }
            } else {
                pipeline().fireChannelRead(msg);
            }
        }

        public void channelReadComplete(ChannelHandlerContext ctx) {
            pipeline().fireChannelReadComplete();
        }

        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            // NOOP
        }

        public void channelWritabilityChanged(ChannelHandlerContext ctx) {
            pipeline().fireChannelWritabilityChanged();
        }

        @SuppressWarnings("deprecation")
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof ClosedChannelException) {
                return;
            }
            pipeline().fireExceptionCaught(cause);
        }

    }

}
