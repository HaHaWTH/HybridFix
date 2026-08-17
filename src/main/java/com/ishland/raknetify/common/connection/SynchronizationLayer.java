/*
 * This file is a part of the Raknetify project, licensed under MIT.
 *
 * Copyright (c) 2022-2025 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.raknetify.common.connection;

import com.ishland.raknetify.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import network.ycc.raknet.frame.Frame;
import network.ycc.raknet.frame.FrameData;
import network.ycc.raknet.packet.FrameSet;
import network.ycc.raknet.packet.FramedPacket;
import network.ycc.raknet.pipeline.FrameJoiner;
import network.ycc.raknet.pipeline.FrameOrderIn;
import network.ycc.raknet.pipeline.FrameOrderOut;
import network.ycc.raknet.pipeline.ReliabilityHandler;
import network.ycc.raknet.utils.UINT;
import org.apache.commons.math3.util.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import static com.ishland.raknetify.common.util.ReflectionUtil.accessible;

public class SynchronizationLayer extends ChannelDuplexHandler {

    // Request structure:
    // byte: total channel count `n`
    // next `n` groups: {
    // byte: channel index
    // integer: current orderIndex (or lastOrderIndex, (nextOrderIndex - 1))
    // }
    // integer: current seqId (or lastReceivedSeqId, (nextSendSeqId - 1))
    //
    // Response callback is handled using reliable transport

    public static final Object SYNC_REQUEST_OBJECT = new Object();

    private static final int BARRIER_EPOCH_TRAILER_MAGIC = 0x48464245; // HFBE

    public static Object synchronizationRequest(int barrierEpoch) {
        return new SynchronizationRequest(barrierEpoch);
    }

    public static boolean isSynchronizationRequest(Object msg) {
        return msg == SYNC_REQUEST_OBJECT || msg instanceof SynchronizationRequest;
    }

    static final Class<?> CLASS_QUEUE;
    static final Class<?> CLASS_FRAME_JOINER_BUILDER;
    static final Field FIELD_QUEUE_LAST_ORDER_INDEX;
    static final Method METHOD_QUEUE_BUILDER_RELEASE;
    static final Method METHOD_QUEUE_CLEAR;
    static final Field FIELD_RELIABILITY_NEXT_SEND_SEQ_ID;
    static final Field FIELD_RELIABILITY_LAST_RECEIVED_SEQ_ID;
    static final Field FIELD_RELIABILITY_QUEUED_BYTES;
    static final Field FIELD_FRAME_JOINER_BUILDER_SAMPLE_PACKET;

    static {
        try {
            CLASS_QUEUE = Class.forName("network.ycc.raknet.pipeline.FrameOrderIn$OrderedChannelPacketQueue");
            CLASS_FRAME_JOINER_BUILDER = Class.forName("network.ycc.raknet.pipeline.FrameJoiner$Builder");

            FIELD_QUEUE_LAST_ORDER_INDEX = accessible(CLASS_QUEUE.getDeclaredField("lastOrderIndex"));
            FIELD_RELIABILITY_NEXT_SEND_SEQ_ID = accessible(ReliabilityHandler.class.getDeclaredField("nextSendSeqId"));
            FIELD_RELIABILITY_LAST_RECEIVED_SEQ_ID = accessible(ReliabilityHandler.class.getDeclaredField("lastReceivedSeqId"));
            FIELD_RELIABILITY_QUEUED_BYTES = accessible(ReliabilityHandler.class.getDeclaredField("queuedBytes"));
            FIELD_FRAME_JOINER_BUILDER_SAMPLE_PACKET = accessible(CLASS_FRAME_JOINER_BUILDER.getDeclaredField("samplePacket"));

            METHOD_QUEUE_BUILDER_RELEASE = accessible(CLASS_FRAME_JOINER_BUILDER.getDeclaredMethod("release"));
            METHOD_QUEUE_CLEAR = accessible(CLASS_QUEUE.getDeclaredMethod("clear"));

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private final IntSet channelToIgnore = new IntOpenHashSet();

    private FrameOrderIn frameOrderIn;
    private Object[] frameOrderInQueues;
    private FrameOrderOut frameOrderOut;
    private int[] frameOrderOutNextOrderIndex;
    private ReliabilityHandler reliabilityHandler;
    private PriorityQueue<Frame> reliabilityHandlerFrameQueue;
    private Int2ObjectMap<FrameSet> reliabilityHandlerPendingFrameSets;
    private FrameJoiner frameJoiner;
    private Int2ObjectOpenHashMap<?> frameJoinerPendingPackets;
    private int channelsLength;
    private boolean initialized = false;

    public SynchronizationLayer(int... channelsToIgnore) {
        for (int ch : channelsToIgnore) {
            channelToIgnore.add(ch);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        initializeIfNecessary(ctx);
    }

    private void initializeIfNecessary(ChannelHandlerContext ctx) {
        if (initialized) return;
        try {
            this.frameOrderIn = ctx.pipeline().get(FrameOrderIn.class);
            Object frameOrderInQueueArray = accessible(FrameOrderIn.class.getDeclaredField("channels")).get(this.frameOrderIn);
            this.frameOrderInQueues = new Object[Array.getLength(frameOrderInQueueArray)];
            for (int i = 0; i < this.frameOrderInQueues.length; i++) {
                this.frameOrderInQueues[i] = Array.get(frameOrderInQueueArray, i);
            }

            this.frameOrderOut = ctx.pipeline().get(FrameOrderOut.class);
            this.frameOrderOutNextOrderIndex = (int[]) accessible(FrameOrderOut.class.getDeclaredField("nextOrderIndex")).get(this.frameOrderOut);

            this.reliabilityHandler = ctx.pipeline().get(ReliabilityHandler.class);
            this.reliabilityHandlerFrameQueue = (PriorityQueue<Frame>) accessible(ReliabilityHandler.class.getDeclaredField("frameQueue")).get(this.reliabilityHandler);
            this.reliabilityHandlerPendingFrameSets = (Int2ObjectMap<FrameSet>) accessible(ReliabilityHandler.class.getDeclaredField("pendingFrameSets")).get(this.reliabilityHandler);

            int originalChannelsLength = this.frameOrderOutNextOrderIndex.length;
            //noinspection deprecation
            this.channelsLength = (int) (originalChannelsLength - this.channelToIgnore.stream().filter(value -> value < originalChannelsLength).count());

            this.frameJoiner = ctx.pipeline().get(FrameJoiner.class);
            this.frameJoinerPendingPackets = (Int2ObjectOpenHashMap<?>) accessible(FrameJoiner.class.getDeclaredField("pendingPackets")).get(this.frameJoiner);

            initialized = true;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        initializeIfNecessary(ctx);
        if (msg instanceof FrameData && ((FrameData) msg).getPacketId() == Constants.RAKNET_SYNC_PACKET_ID) {
            FrameData packet = (FrameData) msg;
            try {
                if (Constants.DEBUG) System.out.println("Raknetify: Received sync packet");
                final ByteBuf byteBuf = packet.createData();
                try {
                    byteBuf.skipBytes(1);
                    final int count = byteBuf.readUnsignedByte();
                    if (count > frameOrderInQueues.length || byteBuf.readableBytes() < count * 5 + 4) {
                        throw new DecoderException("Invalid Raknetify synchronization packet");
                    }

                    final int[] channels = new int[count];
                    final int[] orderIndices = new int[count];
                    for (int i = 0; i < count; i++) {
                        final int channel = byteBuf.readUnsignedByte();
                        if (channel >= frameOrderInQueues.length) {
                            throw new DecoderException("Invalid synchronization channel " + channel);
                        }
                        channels[i] = channel;
                        orderIndices[i] = byteBuf.readInt();
                    }
                    final int seqId = byteBuf.readInt();

                    int barrierEpoch = -1;
                    if (byteBuf.readableBytes() >= 8
                            && byteBuf.readInt() == BARRIER_EPOCH_TRAILER_MAGIC) {
                        barrierEpoch = byteBuf.readInt();
                    }

                    if (barrierEpoch < 0) {
                        if (legacySyncSeen
                                && UINT.B3.minusWrap(seqId, lastLegacySyncSeqId) <= 0) {
                            return; // duplicate or stale legacy reliable-unordered sync packet
                        }
                        legacySyncSeen = true;
                        lastLegacySyncSeqId = seqId;
                    }

                    final CustomPayloadOrderBarrier barrier = ctx.pipeline().get(CustomPayloadOrderBarrier.class);
                    if (barrier != null) {
                        if (barrierEpoch >= 0) {
                            if (!barrier.resetForSynchronization(barrierEpoch)) {
                                return; // duplicate or stale reliable-unordered sync packet
                            }
                        } else {
                            barrier.resetForSynchronization();
                        }
                    }

                    ctx.fireUserEventTriggered(SYNC_REQUEST_OBJECT);
                    for (int i = 0; i < count; i++) {
                        final int channel = channels[i];
                        final int orderIndex = orderIndices[i];
                        if (Constants.DEBUG)
                            System.out.println(String.format("Raknetify: Channel %d: %d -> %d",
                                    channel,
                                            (int) FIELD_QUEUE_LAST_ORDER_INDEX.get(frameOrderInQueues[channel]),
                                            orderIndex
                                    ));
                        METHOD_QUEUE_CLEAR.invoke(frameOrderInQueues[channel]);
                        FIELD_QUEUE_LAST_ORDER_INDEX.set(frameOrderInQueues[channel], orderIndex);
                        final ObjectIterator<?> iterator = this.frameJoinerPendingPackets.values().iterator();
                        while (iterator.hasNext()) {
                            final Object next = iterator.next();
                            try {
                                final Frame frame = (Frame) FIELD_FRAME_JOINER_BUILDER_SAMPLE_PACKET.get(next);
                                if (frame.getReliability().isOrdered && frame.getOrderChannel() == channel) {
                                    METHOD_QUEUE_BUILDER_RELEASE.invoke(next);
                                    iterator.remove();
                                }
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (Constants.DEBUG)
                        System.out.println(String.format("Raknetify: ReliabilityHandler: %d -> %d",
                                (int) FIELD_RELIABILITY_LAST_RECEIVED_SEQ_ID.get(this.reliabilityHandler),
                                seqId
                        ));
                    FIELD_RELIABILITY_LAST_RECEIVED_SEQ_ID.set(this.reliabilityHandler, seqId);
                } finally {
                    byteBuf.release();
                }
            } finally {
                packet.release();
            }
            return;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        cleanupPendingWrites(new ClosedChannelException());
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cleanupPendingWrites(new ClosedChannelException());
        super.channelInactive(ctx);
    }

    private final ReferenceLinkedOpenHashSet<Pair<ChannelPromise, Object>> queue = new ReferenceLinkedOpenHashSet<>();
    private final ObjectArrayList<Frame> queuedFrames = new ObjectArrayList<>();
    private boolean isWaitingForResponse = false;
    private boolean legacySyncSeen;
    private int lastLegacySyncSeqId;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        initializeIfNecessary(ctx);
        if (isSynchronizationRequest(msg)) {
            if (isWaitingForResponse) {
                promise.trySuccess();
                return;
            }

            final int barrierEpoch = msg instanceof SynchronizationRequest
                    ? ((SynchronizationRequest) msg).barrierEpoch
                    : -1;
            ByteBuf byteBuf = null;
            FrameData frameData = null;
            boolean frameTransferred = false;
            try {
                dropSenderPackets();

                byteBuf = ctx.alloc().buffer(
                        1 + channelsLength * 5 + 4 + (barrierEpoch >= 0 ? 8 : 0));
                byteBuf.writeByte(channelsLength);
                for (int channel = 0, frameOrderOutNextOrderIndexLength = frameOrderOutNextOrderIndex.length; channel < frameOrderOutNextOrderIndexLength; channel++) {
                    if (channelToIgnore.contains(channel)) continue;
                    int orderOutNextOrderIndex = frameOrderOutNextOrderIndex[channel];
                    if (Constants.DEBUG)
                        System.out.println(String.format("Raknetify: Writing sync packet: Channel %d: %d", channel, orderOutNextOrderIndex - 1));
                    byteBuf.writeByte(channel);
                    byteBuf.writeInt(orderOutNextOrderIndex - 1);
                }
                int seqId = (int) FIELD_RELIABILITY_NEXT_SEND_SEQ_ID.get(this.reliabilityHandler); // TODO implementation details (probable lib bug): nextSendSeqId == lastReceivedSeqId
                if (Constants.DEBUG)
                    System.out.println(String.format("Raknetify: Writing sync packet: ReliabilityHandler: %d", seqId));
                byteBuf.writeInt(seqId);
                if (barrierEpoch >= 0) {
                    byteBuf.writeInt(BARRIER_EPOCH_TRAILER_MAGIC);
                    byteBuf.writeInt(barrierEpoch);
                }

                frameData = FrameData.create(ctx.alloc(), Constants.RAKNET_SYNC_PACKET_ID, byteBuf);
                frameData.setReliability(FramedPacket.Reliability.RELIABLE);
                this.isWaitingForResponse = true;
                final io.netty.channel.ChannelFuture syncFuture = ctx.write(frameData);
                frameTransferred = true;
                syncFuture.addListener(future -> {
                    if (future.isSuccess()) {
                        this.flushQueue(ctx);
                    } else {
                        Throwable failureCause = future.cause();
                        if (failureCause == null) {
                            failureCause = new ClosedChannelException();
                        }
                        final Throwable cause = failureCause;
                        this.failSynchronization(ctx, cause);
                    }
                });
                promise.trySuccess();
            } catch (RuntimeException | Error t) {
                cleanupPendingWrites(t);
                promise.tryFailure(t);
                throw t;
            } finally {
                ReferenceCountUtil.safeRelease(byteBuf);
                if (!frameTransferred) {
                    ReferenceCountUtil.safeRelease(frameData);
                }
            }
            return;
        }
        if (isWaitingForResponse) {
            this.queue.add(Pair.create(promise, msg));
            return;
        }
        super.write(ctx, msg, promise);
    }

    private void dropSenderPackets() {
        int droppedFrames = 0;

        ArrayList<Frame> retainedFrameList = new ArrayList<>();

        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        retainedFrameList.addAll(this.reliabilityHandlerFrameQueue);
        this.reliabilityHandlerFrameQueue.clear();

        for (FrameSet frameSet : this.reliabilityHandlerPendingFrameSets.values()) {
            frameSet.createFrames(retainedFrameList::add);
            frameSet.release();
        }
        this.reliabilityHandlerPendingFrameSets.clear();

        int byteSize = 0;
        for (Iterator<Frame> iterator = retainedFrameList.iterator(); iterator.hasNext(); ) {
            Frame frame = iterator.next();
            if (frame.getReliability().isOrdered && !channelToIgnore.contains(frame.getOrderChannel())) {
                final ChannelPromise promise1 = frame.getPromise();
                if (promise1 != null) promise1.trySuccess();
                iterator.remove();
                frame.release();
                droppedFrames++;
            } else {
                byteSize += frame.getRoughPacketSize();
            }
        }
        this.queuedFrames.addAll(retainedFrameList);

        if (Constants.DEBUG) System.out.println(String.format("Raknetify: Dropping %d frames", droppedFrames));
        try {
            FIELD_RELIABILITY_QUEUED_BYTES.set(this.reliabilityHandler, byteSize);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        this.reliabilityHandlerPendingFrameSets.clear();
    }

    private void flushQueue(ChannelHandlerContext ctx) {
        if (!isWaitingForResponse) {
            if (Constants.DEBUG) System.out.println("Raknetify: Ignoring duplicate call to flushQueue()");
            return;
        }
        if (!ctx.channel().eventLoop().inEventLoop()) {
            ctx.channel().eventLoop().execute(() -> flushQueue(ctx));
            return;
        }

        this.isWaitingForResponse = false;

        if (Constants.DEBUG) System.out.println(String.format("Raknetify: Picking up %d queued frames", this.queuedFrames.size()));
        this.reliabilityHandlerFrameQueue.addAll(this.queuedFrames);
        this.queuedFrames.clear();

        if (Constants.DEBUG) System.out.println(String.format("Raknetify: Flushing %d queued packets as synchronization finished", this.queue.size()));
        while (!this.queue.isEmpty()) {
            Pair<ChannelPromise, Object> pair = this.queue.removeFirst();
            final ChannelPromise promise = pair.getFirst();
            final Object msg = pair.getSecond();
            boolean handedOff = false;
            try {
                ctx.write(msg, promise);
                handedOff = true;
            } catch (Throwable t) {
                promise.tryFailure(t);
                if (!handedOff) {
                    ReferenceCountUtil.safeRelease(msg);
                }
                cleanupPendingWrites(t);
                try {
                    ctx.fireExceptionCaught(t);
                } finally {
                    ctx.close();
                }
                return;
            }
        }
    }

    private void failSynchronization(ChannelHandlerContext ctx, Throwable cause) {
        if (!ctx.channel().eventLoop().inEventLoop()) {
            ctx.channel().eventLoop().execute(() -> failSynchronization(ctx, cause));
            return;
        }
        if (!isWaitingForResponse) {
            return;
        }
        cleanupPendingWrites(cause);
        ctx.close();
    }

    private void cleanupPendingWrites(Throwable cause) {
        final boolean hadSynchronizationState = isWaitingForResponse || !this.queuedFrames.isEmpty();
        isWaitingForResponse = false;

        while (!this.queue.isEmpty()) {
            Pair<ChannelPromise, Object> pair = this.queue.removeFirst();
            try {
                pair.getFirst().tryFailure(cause);
            } finally {
                ReferenceCountUtil.safeRelease(pair.getSecond());
            }
        }

        for (Frame frame : this.queuedFrames) {
            try {
                final ChannelPromise promise = frame.getPromise();
                if (promise != null) {
                    promise.tryFailure(cause);
                    frame.setPromise(null);
                }
            } finally {
                ReferenceCountUtil.safeRelease(frame);
            }
        }
        this.queuedFrames.clear();

        if (hadSynchronizationState && this.reliabilityHandler != null) {
            try {
                FIELD_RELIABILITY_QUEUED_BYTES.set(this.reliabilityHandler, 0);
            } catch (Throwable resetFailure) {
                if (cause != resetFailure) {
                    cause.addSuppressed(resetFailure);
                }
            }
        }
    }

    private static final class SynchronizationRequest {
        private final int barrierEpoch;

        private SynchronizationRequest(int barrierEpoch) {
            this.barrierEpoch = barrierEpoch;
        }
    }

}
