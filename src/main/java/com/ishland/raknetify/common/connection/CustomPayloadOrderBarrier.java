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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.PromiseCombiner;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import network.ycc.raknet.frame.FrameData;
import network.ycc.raknet.packet.FramedPacket;

import java.util.ArrayDeque;

/**
 * Makes channel 7 payload wait until every ordered channel has delivered all
 * frames which preceded it at the sender. Other channels are never paused.
 */
public final class CustomPayloadOrderBarrier extends ChannelInboundHandlerAdapter {

    public static final String NAME = "raknetify-custom-payload-order-barrier";
    public static final int CHANNEL_OVERRIDE = -3;
    public static final int TARGET_CHANNEL = 7;

    private static final int CHANNEL_COUNT = 8;
    private static final int ALL_CHANNELS_MASK = (1 << CHANNEL_COUNT) - 1;
    private static final int MAX_PENDING_BARRIERS = 1024;
    private static final int EPOCH_BITS = 12;
    private static final int SEQUENCE_BITS = Integer.SIZE - EPOCH_BITS;
    private static final int EPOCH_MASK = (1 << EPOCH_BITS) - 1;
    private static final int EPOCH_HALF_RANGE = 1 << (EPOCH_BITS - 1);
    private static final int SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;

    private final Int2IntOpenHashMap receivedChannels = new Int2IntOpenHashMap();
    private final ArrayDeque<Object> channel7Queue = new ArrayDeque<>();
    private long queuedBytes;
    private int inboundEpoch;
    private boolean inboundEpochEnforced;

    public static int composeBarrierId(int epoch, int sequence) {
        return ((epoch & EPOCH_MASK) << SEQUENCE_BITS) | (sequence & SEQUENCE_MASK);
    }

    public static int extractEpoch(int barrierId) {
        return (barrierId >>> SEQUENCE_BITS) & EPOCH_MASK;
    }

    public static int nextEpoch(int epoch) {
        return (epoch + 1) & EPOCH_MASK;
    }

    /**
     * Writes one marker to every ordered channel and returns a future which is
     * completed after all marker writes complete. The returned future may be
     * shared by multiple channel 7 payloads while no new ordered frame is
     * written to channels 0-6.
     */
    public static ChannelFuture writeBarrierMarkers(ChannelHandlerContext ctx, int barrierId) {
        PromiseCombiner combiner = new PromiseCombiner();
        ChannelPromise barrierPromise = ctx.newPromise();
        try {
            for (int channel = 0; channel < CHANNEL_COUNT; channel++) {
                ByteBuf markerPayload = ctx.alloc().buffer(4, 4);
                FrameData marker = null;
                try {
                    markerPayload.writeInt(barrierId);
                    marker = FrameData.create(ctx.alloc(),
                            Constants.RAKNET_CUSTOM_PAYLOAD_BARRIER_PACKET_ID, markerPayload);
                    marker.setReliability(FramedPacket.Reliability.RELIABLE_ORDERED);
                    marker.setOrderChannel(channel);
                    ChannelFuture markerFuture = ctx.write(marker);
                    marker = null;
                    markerFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    combiner.add(markerFuture);
                } finally {
                    markerPayload.release();
                    ReferenceCountUtil.safeRelease(marker);
                }
            }
            combiner.finish(barrierPromise);
            return barrierPromise;
        } catch (RuntimeException | Error t) {
            barrierPromise.tryFailure(t);
            // Some marker writes may already have reached the peer. A partial
            // marker group cannot be repaired by a later barrier ID.
            ctx.close();
            throw t;
        }
    }

    /**
     * Writes a payload whose delivery depends on an existing marker group.
     * This method takes ownership of {@code payload}, including when the write
     * fails synchronously.
     */
    public static void writeAfterBarrier(ChannelHandlerContext ctx, FrameData payload,
            ChannelPromise promise, ChannelFuture barrierFuture) {
        PromiseCombiner combiner = new PromiseCombiner();
        boolean payloadTransferred = false;
        try {
            ChannelFuture payloadFuture = ctx.write(payload);
            payloadTransferred = true;
            combiner.add(barrierFuture);
            combiner.add(payloadFuture);
            combiner.finish(promise);
        } catch (RuntimeException | Error t) {
            promise.tryFailure(t);
            throw t;
        } finally {
            if (!payloadTransferred) {
                ReferenceCountUtil.safeRelease(payload);
            }
        }
    }

    @Deprecated
    public static void writeBarrier(ChannelHandlerContext ctx, FrameData payload,
            ChannelPromise promise, int barrierId) {
        final ChannelFuture barrierFuture;
        try {
            barrierFuture = writeBarrierMarkers(ctx, barrierId);
        } catch (RuntimeException | Error t) {
            promise.tryFailure(t);
            ReferenceCountUtil.safeRelease(payload);
            throw t;
        }
        writeAfterBarrier(ctx, payload, promise, barrierFuture);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FrameData)) {
            super.channelRead(ctx, msg);
            return;
        }

        FrameData frameData = (FrameData) msg;
        if (!frameData.isFragment() && frameData.getPacketId() == Constants.RAKNET_CUSTOM_PAYLOAD_BARRIER_PACKET_ID) {
            try {
                receiveMarker(ctx, frameData);
            } finally {
                frameData.release();
            }
            return;
        }

        if (frameData.getOrderChannel() == TARGET_CHANNEL && !channel7Queue.isEmpty()) {
            long newQueuedBytes = queuedBytes + frameData.getDataSize();
            if (newQueuedBytes > Constants.MAX_QUEUED_SIZE) {
                frameData.release();
                throw new DecoderException("Too much channel 7 data waiting for custom-payload barriers");
            }
            queuedBytes = newQueuedBytes;
            frameData.touch("Waiting for preceding custom-payload barrier markers");
            channel7Queue.addLast(frameData);
            return;
        }

        super.channelRead(ctx, msg);
    }

    private void receiveMarker(ChannelHandlerContext ctx, FrameData marker) {
        if (marker.getReliability() != FramedPacket.Reliability.RELIABLE_ORDERED
                || marker.getOrderChannel() < 0 || marker.getOrderChannel() >= CHANNEL_COUNT
                || marker.getDataSize() != 5) {
            throw new DecoderException("Invalid custom-payload barrier marker");
        }

        ByteBuf data = marker.createData();
        final int barrierId;
        try {
            data.skipBytes(1);
            barrierId = data.readInt();
        } finally {
            data.release();
        }

        if (inboundEpochEnforced && extractEpoch(barrierId) != inboundEpoch) {
            return;
        }

        int oldMask = receivedChannels.get(barrierId);
        int channelBit = 1 << marker.getOrderChannel();
        if ((oldMask & channelBit) != 0) {
            return;
        }
        if (oldMask == 0 && receivedChannels.size() >= MAX_PENDING_BARRIERS) {
            throw new DecoderException("Too many pending custom-payload barriers");
        }

        receivedChannels.put(barrierId, oldMask | channelBit);
        if (marker.getOrderChannel() == TARGET_CHANNEL) {
            channel7Queue.addLast(new BarrierGate(barrierId));
        }
        drainChannel7(ctx);
    }

    private void drainChannel7(ChannelHandlerContext ctx) {
        while (!channel7Queue.isEmpty()) {
            Object next = channel7Queue.peekFirst();
            if (next instanceof BarrierGate) {
                int barrierId = ((BarrierGate) next).barrierId;
                int mask = receivedChannels.get(barrierId);
                if (mask != ALL_CHANNELS_MASK) {
                    return;
                }
                channel7Queue.removeFirst();
                receivedChannels.remove(barrierId);
            } else {
                FrameData frameData = (FrameData) channel7Queue.removeFirst();
                queuedBytes -= frameData.getDataSize();
                ctx.fireChannelRead(frameData);
            }
        }
    }

    /**
     * Applies a synchronization epoch exactly once. Duplicate or stale sync
     * packets are ignored, including after the new epoch has started receiving
     * barrier markers.
     *
     * @return {@code true} if the epoch was advanced and pending data discarded
     */
    public boolean resetForSynchronization(int epoch) {
        int normalizedEpoch = epoch & EPOCH_MASK;
        int distance = (normalizedEpoch - inboundEpoch) & EPOCH_MASK;
        if (distance == 0 || distance >= EPOCH_HALF_RANGE) {
            return false;
        }

        inboundEpoch = normalizedEpoch;
        inboundEpochEnforced = true;
        releaseQueue();
        return true;
    }

    /**
     * Compatibility fallback for sync packets without an epoch trailer. It can
     * only discard pending state; the peer continues using its current epoch.
     */
    public void resetForSynchronization() {
        releaseQueue();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        releaseQueue();
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        releaseQueue();
        super.channelInactive(ctx);
    }

    private void releaseQueue() {
        Object queued;
        while ((queued = channel7Queue.pollFirst()) != null) {
            ReferenceCountUtil.safeRelease(queued);
        }
        receivedChannels.clear();
        queuedBytes = 0L;
    }

    private static final class BarrierGate {
        private final int barrierId;

        private BarrierGate(int barrierId) {
            this.barrierId = barrierId;
        }
    }
}
