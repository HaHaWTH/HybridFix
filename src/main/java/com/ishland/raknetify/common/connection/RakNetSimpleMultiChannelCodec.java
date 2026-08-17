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
import com.ishland.raknetify.common.util.MathUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import network.ycc.raknet.frame.FrameData;
import network.ycc.raknet.packet.FramedPacket;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RakNetSimpleMultiChannelCodec extends ChannelDuplexHandler {

    public static final String NAME = "raknetify-simple-multi-channel-data-codec";

    public static final Object SIGNAL_START_MULTICHANNEL = new Object();

    private final int packetId;

    public RakNetSimpleMultiChannelCodec(int packetId) {
        this.packetId = packetId;
    }

    private final ObjectArrayList<OverrideHandler> handlers = new ObjectArrayList<>();

    public RakNetSimpleMultiChannelCodec addHandler(OverrideHandler handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
        return this;
    }

    public void removeHandler(OverrideHandler handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    public <T> T getHandler(Class<T> clazz) {
        synchronized (handlers) {
            for (OverrideHandler handler : handlers) {
                if (clazz.isInstance(handler)) return clazz.cast(handler);
            }
        }
        return null;
    }

    private boolean isMultichannelEnabled;

    private boolean queuePendingWrites = false;
    private final Queue<PendingWrite> pendingWrites = new LinkedList<>();
    private int outboundBarrierEpoch;
    private int nextCustomPayloadBarrierSequence;
    // Proof that no channel 0-6 ordered game frame has been written since this
    // marker group. Channel 7 keeps its own order; unordered frames were never
    // covered by a barrier in the first place.
    private ChannelFuture reusableCustomPayloadBarrier;
    private boolean handlerUnavailable;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.handlerUnavailable = true;
        failPendingWrites(new IllegalStateException("Channel closed"));
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.handlerUnavailable = true;
        failPendingWrites(new IllegalStateException("Channel closed"));
        super.channelInactive(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (this.queuePendingWrites && msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            EncodedFrame encoded = null;
            boolean queued = false;
            try {
                encoded = encode0(ctx, buf);
                if (encoded != null) {
                    pendingWrites.add(new PendingWrite(encoded.frameData,
                            encoded.waitForPriorChannels, promise));
                    queued = true;
                } else {
                    promise.trySuccess();
                }
            } catch (RuntimeException | Error t) {
                if (encoded != null && !queued) {
                    ReferenceCountUtil.safeRelease(encoded.frameData);
                }
                promise.tryFailure(t);
                throw t;
            } finally {
                buf.release();
            }
            return;
        }

        if (msg == SIGNAL_START_MULTICHANNEL) {
            promise.trySuccess();
            if (this.isMultichannelEnabled) return;
            if (!this.isMultichannelAvailable()) {
                System.out.println("Raknetify: [MultiChannellingDataCodec] Failed to start multichannel: not available");
                return;
            }
            invalidateReusableCustomPayloadBarrier();
            final ByteBuf buf = ctx.alloc().buffer(1).writeByte(0);
            try {
                final FrameData frameData = FrameData.create(ctx.alloc(), Constants.RAKNET_PING_PACKET_ID, buf);
                frameData.setOrderChannel(7);
                this.queuePendingWrites = true;
                boolean handedOff = false;
                try {
                    final ChannelFuture future = ctx.write(frameData);
                    handedOff = true;
                    future.addListener(result -> {
                        if (!result.isSuccess() || handlerUnavailable || !ctx.channel().isActive()) {
                            Throwable cause = result.cause();
                            if (cause == null) {
                                cause = new IllegalStateException("Channel closed before multichannel startup completed");
                            }
                            failPendingWrites(cause);
                            return;
                        }

                        isMultichannelEnabled = true;
                        if (Constants.DEBUG) System.out.println("Raknetify: [MultiChannellingDataCodec] Started multichannel");
                        flushPendingWrites(ctx);
                    });
                } catch (RuntimeException | Error t) {
                    failPendingWrites(t);
                    throw t;
                } finally {
                    if (!handedOff) {
                        ReferenceCountUtil.safeRelease(frameData);
                    }
                }
            } finally {
                buf.release();
            }
            return;
        }
        if (msg == SynchronizationLayer.SYNC_REQUEST_OBJECT) {
            invalidateReusableCustomPayloadBarrier();
            if (this.isMultichannelEnabled) {
                if (Constants.DEBUG) System.out.println("Raknetify: [MultiChannellingDataCodec] Stopped multichannel");
                this.isMultichannelEnabled = false;
                this.outboundBarrierEpoch = CustomPayloadOrderBarrier.nextEpoch(this.outboundBarrierEpoch);
                this.nextCustomPayloadBarrierSequence = 0;
                super.write(ctx, SynchronizationLayer.synchronizationRequest(this.outboundBarrierEpoch), promise);
                return;
            }
            promise.trySuccess();
            return; // discard sync request when multichannel is not active
        }

        if (msg instanceof ByteBuf && ((ByteBuf) msg).isReadable()) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                final EncodedFrame encoded = encode0(ctx, buf);
                if (encoded != null) {
                    writeEncoded(ctx, encoded.frameData, encoded.waitForPriorChannels, promise);
                } else {
                    promise.trySuccess();
                }
            } finally {
                buf.release();
            }
            return;
        }

        if (msg instanceof FrameData && invalidatesReusableCustomPayloadBarrier((FrameData) msg)) {
            invalidateReusableCustomPayloadBarrier();
        }
        super.write(ctx, msg, promise);
    }

    private EncodedFrame encode0(ChannelHandlerContext ctx, ByteBuf buf) {
        if (buf.isReadable()) {
            final int packetChannelOverride = getChannelOverride(buf, !isMultichannelEnabled);
            if (packetChannelOverride == Integer.MIN_VALUE) {
                return null; // the void
            }
            final FrameData frameData = FrameData.create(ctx.alloc(), packetId, buf);
            boolean waitForPriorChannels = false;
            if (isMultichannelEnabled) {
                if (packetChannelOverride >= 0)
                    frameData.setOrderChannel(packetChannelOverride);
                else if (packetChannelOverride == CustomPayloadOrderBarrier.CHANNEL_OVERRIDE) {
                    frameData.setOrderChannel(CustomPayloadOrderBarrier.TARGET_CHANNEL);
                    waitForPriorChannels = true;
                }
                else if (packetChannelOverride == -1)
                    frameData.setReliability(FramedPacket.Reliability.RELIABLE);
                else if (packetChannelOverride == -2)
                    frameData.setReliability(FramedPacket.Reliability.UNRELIABLE);
            }
            return new EncodedFrame(frameData, waitForPriorChannels);
        }
        return null;
    }

    private void flushPendingWrites(ChannelHandlerContext ctx) {
        if (this.handlerUnavailable || !ctx.channel().isActive()) {
            failPendingWrites(new IllegalStateException("Channel closed"));
            return;
        }

        this.queuePendingWrites = false;
        PendingWrite pendingWrite;
        while ((pendingWrite = this.pendingWrites.poll()) != null) {
            try {
                writeEncoded(ctx, pendingWrite.frameData, pendingWrite.waitForPriorChannels, pendingWrite.promise);
            } catch (Throwable t) {
                // writeEncoded takes ownership of frameData on both success and failure.
                pendingWrite.promise.tryFailure(t);
                ctx.fireExceptionCaught(t);
            }
        }
    }

    private void failPendingWrites(Throwable cause) {
        this.queuePendingWrites = false;
        this.isMultichannelEnabled = false;
        invalidateReusableCustomPayloadBarrier();
        PendingWrite pendingWrite;
        while ((pendingWrite = this.pendingWrites.poll()) != null) {
            pendingWrite.promise.tryFailure(cause);
            ReferenceCountUtil.safeRelease(pendingWrite.frameData);
        }
    }

    /** Takes ownership of {@code frameData}, including when the write fails. */
    private void writeEncoded(ChannelHandlerContext ctx, FrameData frameData, boolean waitForPriorChannels, ChannelPromise promise) {
        if (waitForPriorChannels) {
            final ChannelFuture barrierFuture;
            try {
                barrierFuture = getOrCreateCustomPayloadBarrier(ctx);
            } catch (RuntimeException | Error t) {
                promise.tryFailure(t);
                ReferenceCountUtil.safeRelease(frameData);
                throw t;
            }
            CustomPayloadOrderBarrier.writeAfterBarrier(ctx, frameData, promise, barrierFuture);
        } else {
            if (invalidatesReusableCustomPayloadBarrier(frameData)) {
                invalidateReusableCustomPayloadBarrier();
            }
            boolean handedOff = false;
            try {
                ctx.write(frameData, promise);
                handedOff = true;
            } finally {
                if (!handedOff) {
                    ReferenceCountUtil.safeRelease(frameData);
                }
            }
        }
    }

    private ChannelFuture getOrCreateCustomPayloadBarrier(ChannelHandlerContext ctx) {
        ChannelFuture barrierFuture = this.reusableCustomPayloadBarrier;
        if (barrierFuture != null) {
            return barrierFuture;
        }

        final int barrierId = CustomPayloadOrderBarrier.composeBarrierId(this.outboundBarrierEpoch, this.nextCustomPayloadBarrierSequence++);
        barrierFuture = CustomPayloadOrderBarrier.writeBarrierMarkers(ctx, barrierId);
        this.reusableCustomPayloadBarrier = barrierFuture;
        final ChannelFuture createdBarrier = barrierFuture;
        barrierFuture.addListener(future -> {
            if (!future.isSuccess()) {
                if (this.reusableCustomPayloadBarrier == createdBarrier) {
                    this.reusableCustomPayloadBarrier = null;
                }
                // A peer may already be waiting behind a subset of this marker
                // group. Continuing with a new barrier cannot release that gate.
                ctx.close();
            }
        });
        return barrierFuture;
    }

    private static boolean invalidatesReusableCustomPayloadBarrier(FrameData frameData) {
        return frameData.getReliability().isOrdered && frameData.getOrderChannel() >= 0
                && frameData.getOrderChannel() < CustomPayloadOrderBarrier.TARGET_CHANNEL;
    }

    private void invalidateReusableCustomPayloadBarrier() {
        this.reusableCustomPayloadBarrier = null;
    }

    protected boolean isMultichannelAvailable() {
        synchronized (handlers) {
            return !handlers.isEmpty();
        }
    }

    protected int getChannelOverride(ByteBuf buf, boolean suppressWarning) {
        synchronized (handlers) {
            for (OverrideHandler handler : handlers) {
                final int override = handler.getChannelOverride(buf, suppressWarning);
                if (override != 0) return override;
            }
        }
        return 0;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FrameData && !((FrameData) msg).isFragment()
                && ((FrameData) msg).getDataSize() > 0) {
            FrameData packet = (FrameData) msg;
            try {
                if (packetId == packet.getPacketId()) {
                    ctx.fireChannelRead(packet.createData().skipBytes(1));
                } else if (packet.getPacketId() == Constants.RAKNET_PING_PACKET_ID) {
                    return;
                } else {
                    ctx.fireChannelRead(packet.retain());
                }
            } finally {
                packet.release();
            }
            return;
        }
        super.channelRead(ctx, msg);
    }

    protected void decode(ChannelHandlerContext ctx, FrameData packet, List<Object> out) {
        assert !packet.isFragment();
        if (packet.getDataSize() > 0) {
            if (packetId == packet.getPacketId()) {
                out.add(packet.createData().skipBytes(1));
            } else if (packet.getPacketId() == Constants.RAKNET_PING_PACKET_ID) {
                return;
            } else {
                out.add(packet.retain());
            }
        }
    }

    public interface OverrideHandler {
        int getChannelOverride(ByteBuf buf, boolean suppressWarning);
    }

    public static class PacketIdBasedOverrideHandler implements OverrideHandler {

        private final IntOpenHashSet unknownPacketIds = new IntOpenHashSet();
        private final Int2IntOpenHashMap channelMapping;
        private final String descriptiveProtocolStatus;

        public PacketIdBasedOverrideHandler(Int2IntMap channelMapping, String descriptiveProtocolStatus) {
            this.channelMapping = new Int2IntOpenHashMap(channelMapping);
            this.descriptiveProtocolStatus = descriptiveProtocolStatus;
        }

        @Override
        public int getChannelOverride(ByteBuf buf, boolean suppressWarning) {
            final ByteBuf slice = buf.slice();
            final int packetId = MathUtil.readVarInt(slice);
            final int override = this.channelMapping.get(packetId);
            if (override == Integer.MAX_VALUE) {
                if (!suppressWarning) {
                    if (this.unknownPacketIds.add(packetId)) {
                        System.err.println(String.format("Raknetify: Unknown packet id %d for %s", packetId, descriptiveProtocolStatus));
                    }
                }
                return 7;
            }
            return override;
        }
    }

    private static final class PendingWrite {
        private final FrameData frameData;
        private final boolean waitForPriorChannels;
        private final ChannelPromise promise;

        private PendingWrite(FrameData frameData, boolean waitForPriorChannels,
                ChannelPromise promise) {
            this.frameData = frameData;
            this.waitForPriorChannels = waitForPriorChannels;
            this.promise = promise;
        }
    }

    private static final class EncodedFrame {
        private final FrameData frameData;
        private final boolean waitForPriorChannels;

        private EncodedFrame(FrameData frameData, boolean waitForPriorChannels) {
            this.frameData = frameData;
            this.waitForPriorChannels = waitForPriorChannels;
        }
    }

}
