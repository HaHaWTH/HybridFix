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
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import network.ycc.raknet.frame.FrameData;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class MultiChannelingStreamingCompression extends ChannelDuplexHandler {

    public static final String NAME = "raknetify-multichannel-streaming-compression";

    public static final long SERVER_HANDSHAKE = 0x40000010;
    public static final long CHANNEL_START = 0x40000012;
    public static final long ZSTD_HELLO = 0x48465A5354440001L;
    public static final long ZSTD_ACK = 0x48465A5354440002L;
    public static final long ZSTD_CHANNEL_START = 0x48465A5354440003L;
    private static final int MAX_ZSTD_DECOMPRESSED_SIZE = 16 * 1024 * 1024;
    private final Inflater[] inflaters = new Inflater[8];
    private final Deflater[] deflaters = new Deflater[8];

    private final IntOpenHashSet channelsToIgnoreWhenReinit = new IntOpenHashSet();

    private final byte[] inflateBuffer = new byte[256 * 1024];
    private final byte[] deflateBuffer = new byte[256 * 1024];

    private final int rawPacketId;
    private final int compressedPacketId;
    private final int zstdPacketId;
    private final boolean zstdSupported;
    private final int zstdLevel;
    private final int zstdThreshold;
    private final boolean[] zstdInbound = new boolean[8];
    private final boolean[] zstdOutbound = new boolean[8];

    private volatile long outBytesRaw = 0L;
    private volatile long outBytesCompressed = 0L;
    private volatile long inBytesCompressed = 0L;
    private volatile long inBytesRaw = 0L;

    private boolean active = false;
    private boolean zstdHelloSent;
    private boolean zstdAckSent;
    private boolean zstdStartsSent;
    private volatile boolean zstdActive;

    public MultiChannelingStreamingCompression(int rawPacketId, int compressedPacketId) {
        this(rawPacketId, compressedPacketId, Constants.RAKNET_ZSTD_COMPRESSION_PACKET_ID,
                true, 3, 256);
    }

    public MultiChannelingStreamingCompression(int rawPacketId, int compressedPacketId,
            int zstdPacketId, boolean zstdEnabled, int zstdLevel, int zstdThreshold) {
        this.rawPacketId = rawPacketId;
        this.compressedPacketId = compressedPacketId;
        this.zstdPacketId = zstdPacketId;
        this.zstdSupported = zstdEnabled && ZstdCompressionUtil.isAvailable();
        this.zstdLevel = zstdLevel;
        this.zstdThreshold = Math.max(16, zstdThreshold);
    }

    private void doServerHandshake(ChannelHandlerContext ctx) {
        final ByteBuf buf = ctx.alloc().buffer().writeLong(SERVER_HANDSHAKE);
        try {
            final FrameData data = FrameData.create(ctx.alloc(), Constants.RAKNET_STREAMING_COMPRESSION_HANDSHAKE_PACKET_ID,
                    buf);
            ctx.write(data);
        } finally {
            buf.release();
        }
    }

    private void doChannelStart(ChannelHandlerContext ctx) {
        if (!active) return;
        ByteBuf buf = ctx.alloc().buffer().writeLong(CHANNEL_START);
        try {
            for (int i = 0; i < 8; i++) {
                final FrameData data = FrameData.create(ctx.alloc(), Constants.RAKNET_STREAMING_COMPRESSION_HANDSHAKE_PACKET_ID,
                        buf);
                data.setOrderChannel(i);
                ctx.write(data);
                initDeflater(i);
            }
        } finally {
            buf.release();
        }
    }

    private void writeControl(ChannelHandlerContext ctx, long control, int orderChannel) {
        ByteBuf buf = ctx.alloc().buffer(8, 8).writeLong(control);
        try {
            FrameData data = FrameData.create(ctx.alloc(),
                    Constants.RAKNET_STREAMING_COMPRESSION_HANDSHAKE_PACKET_ID, buf);
            data.setOrderChannel(orderChannel);
            ctx.write(data);
        } finally {
            buf.release();
        }
    }

    private boolean peerSupportsProtocolExtension(ChannelHandlerContext ctx) {
        io.netty.channel.Channel channel = ctx.channel();
        while (channel != null) {
            if (Boolean.TRUE.equals(channel.attr(network.ycc.raknet.RakNet.HYBRIDFIX_PROTOCOL_EXTENSION).get())) {
                return true;
            }
            channel = channel.parent();
        }
        return false;
    }

    private void doZstdHello(ChannelHandlerContext ctx) {
        if (!zstdSupported || zstdHelloSent || !peerSupportsProtocolExtension(ctx)) {
            return;
        }
        zstdHelloSent = true;
        writeControl(ctx, ZSTD_HELLO, 0);
    }

    private void doZstdChannelStart(ChannelHandlerContext ctx, boolean force) {
        if (!zstdSupported || (!force && zstdStartsSent)) {
            return;
        }
        for (int i = 0; i < zstdOutbound.length; i++) {
            writeControl(ctx, ZSTD_CHANNEL_START, i);
            zstdOutbound[i] = true;
        }
        zstdStartsSent = true;
        zstdActive = true;
    }

    private void initDeflater(int channel) {
        if (!active) return;
        if (deflaters[channel] != null) deflaters[channel].end();
        deflaters[channel] = new Deflater();
        if (Constants.DEBUG) System.out.println(String.format("Raknetify: Streaming compression deflater for ch%d is ready", channel));
    }

    private void initInflater(int channel) {
        if (!active) return;
        if (inflaters[channel] != null) inflaters[channel].end();
        inflaters[channel] = new Inflater();
        if (Constants.DEBUG) System.out.println(String.format("Raknetify: Streaming compression inflater for ch%d is ready", channel));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        doServerHandshake(ctx);
        doZstdHello(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FrameData) {
            FrameData compressedFrameData = (FrameData) msg;
            if (compressedFrameData.getPacketId() == Constants.RAKNET_STREAMING_COMPRESSION_HANDSHAKE_PACKET_ID) {
                compressedFrameData.touch();
                final int orderChannel = compressedFrameData.getOrderChannel();
                ByteBuf payload = null;
                try {
                    payload = compressedFrameData.createData().skipBytes(1);
                    if (payload.readableBytes() == 8) {
                        final long l = payload.readLong();
                        if (l == CHANNEL_START) {
                            initInflater(orderChannel);
                            return;
                        } else if (l == SERVER_HANDSHAKE) {
                            active = true;
                            doChannelStart(ctx);
                            return;
                        } else if (l == ZSTD_HELLO) {
                            if (zstdSupported && peerSupportsProtocolExtension(ctx) && !zstdAckSent) {
                                zstdAckSent = true;
                                writeControl(ctx, ZSTD_ACK, 0);
                            }
                            return;
                        } else if (l == ZSTD_ACK) {
                            if (zstdSupported && zstdHelloSent) {
                                doZstdChannelStart(ctx, false);
                            }
                            return;
                        } else if (l == ZSTD_CHANNEL_START) {
                            if (zstdSupported && orderChannel >= 0 && orderChannel < zstdInbound.length) {
                                zstdInbound[orderChannel] = true;
                                zstdActive = true;
                            }
                            return;
                        }
                    }
                } finally {
                    compressedFrameData.release();
                    if (payload != null) payload.release();
                }
                // Handshake/control packet ids are reserved and never enter Minecraft's pipeline.
                return;
            } else if (compressedFrameData.getPacketId() == zstdPacketId) {
                compressedFrameData.touch();
                final int orderChannel = compressedFrameData.getOrderChannel();
                if (!isCompressible(compressedFrameData)
                        || !zstdSupported
                        || !zstdInbound[orderChannel]) {
                    compressedFrameData.release();
                    throw new DecoderException("Received zstd Raknetify frame before negotiation");
                }

                final ByteBuf data = compressedFrameData.createData().skipBytes(1);
                ByteBuf out = null;
                FrameData rawFrameData = null;
                try {
                    if (data.readableBytes() < 5) {
                        throw new DecoderException("Truncated zstd Raknetify frame");
                    }
                    int rawLength = data.readInt();
                    if (rawLength <= 0 || rawLength > MAX_ZSTD_DECOMPRESSED_SIZE) {
                        throw new DecoderException("Invalid zstd Raknetify decompressed size: " + rawLength);
                    }

                    byte[] input = new byte[data.readableBytes()];
                    data.readBytes(input);
                    byte[] output;
                    try {
                        output = ZstdCompressionUtil.decompress(input, rawLength);
                    } catch (IllegalArgumentException e) {
                        throw new DecoderException(e);
                    }

                    inBytesCompressed += input.length + 4L;
                    inBytesRaw += output.length;

                    out = ctx.alloc().buffer(output.length, output.length).writeBytes(output);
                    rawFrameData = FrameData.create(ctx.alloc(), rawPacketId, out);
                    rawFrameData.setReliability(compressedFrameData.getReliability());
                    rawFrameData.setOrderChannel(orderChannel);
                    ctx.fireChannelRead(rawFrameData);
                    rawFrameData = null;
                    return;
                } finally {
                    data.release();
                    compressedFrameData.release();
                    if (out != null) out.release();
                    if (rawFrameData != null) rawFrameData.release();
                }
            } else if (compressedFrameData.getPacketId() == compressedPacketId && isCompressible(compressedFrameData) && inflaters[compressedFrameData.getOrderChannel()] != null) {
                compressedFrameData.touch();
                final int orderChannel = compressedFrameData.getOrderChannel();
                final Inflater inflater = inflaters[orderChannel];
                final ByteBuf data = compressedFrameData.createData().skipBytes(1);

                ByteBuf out = null;
                FrameData rawFrameData = null;
                try {
                    byte[] input = new byte[data.readableBytes()];
                    data.getBytes(data.readerIndex(), input);
                    inflater.setInput(input);

                    inBytesCompressed += data.readableBytes();

                    out = ctx.alloc().buffer();
                    {
                        int inflatedBytes;
                        while ((inflatedBytes = inflater.inflate(inflateBuffer)) != 0) {
                            out.writeBytes(inflateBuffer, 0, inflatedBytes);
                        }
                    }

                    inBytesRaw += out.writerIndex();

                    rawFrameData = FrameData.create(ctx.alloc(), rawPacketId, out);
                    rawFrameData.setReliability(compressedFrameData.getReliability());
                    rawFrameData.setOrderChannel(orderChannel);
                    ctx.fireChannelRead(rawFrameData);
                    rawFrameData = null;
                    return;
                } finally {
                    data.release();
                    compressedFrameData.release();
                    if (out != null) out.release();
                    if (rawFrameData != null) rawFrameData.release();
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg == SynchronizationLayer.SYNC_REQUEST_OBJECT) {
            super.write(ctx, msg, promise);
            doChannelStart(ctx);
            if (zstdStartsSent) {
                doZstdChannelStart(ctx, true);
            }
            return;
        } else if (msg instanceof FrameData) {
            FrameData rawFrameData = (FrameData) msg;
            rawFrameData.touch();
            if (rawFrameData.getPacketId() == rawPacketId && isCompressible(rawFrameData)
                    && zstdOutbound[rawFrameData.getOrderChannel()]) {
                writeZstd(ctx, rawFrameData, promise);
                return;
            } else if (rawFrameData.getPacketId() == rawPacketId && isCompressible(rawFrameData) && deflaters[rawFrameData.getOrderChannel()] != null) {
                if (rawFrameData.getDataSize() < 16 + 1) {
                    outBytesRaw += rawFrameData.getDataSize() - 1;
                    outBytesCompressed += rawFrameData.getDataSize() - 1;
                    ctx.write(rawFrameData, promise);
                    return;
                }

                final int orderChannel = rawFrameData.getOrderChannel();
                final Deflater deflater = deflaters[orderChannel];
                final ByteBuf data = rawFrameData.createData().skipBytes(1);

                ByteBuf out = null;
                FrameData compressedFrameData = null;
                try {
                    byte[] input = new byte[data.readableBytes()];
                    data.getBytes(data.readerIndex(), input);
                    deflater.setInput(input);

                    outBytesRaw += data.readableBytes();

                    out = ctx.alloc().buffer();
                    {
                        int deflatedBytes;
                        while ((deflatedBytes = deflater.deflate(deflateBuffer, 0, deflateBuffer.length, Deflater.SYNC_FLUSH)) != 0) {
                            out.writeBytes(deflateBuffer, 0, deflatedBytes);
                        }
                    }

                    outBytesCompressed += out.writerIndex();

                    compressedFrameData = FrameData.create(ctx.alloc(), compressedPacketId, out);
                    compressedFrameData.setReliability(rawFrameData.getReliability());
                    compressedFrameData.setOrderChannel(orderChannel);
                    ctx.write(compressedFrameData, promise);
                    compressedFrameData = null;
                    return;
                } finally {
                    data.release();
                    rawFrameData.release();
                    if (out != null) out.release();
                    if (compressedFrameData != null) compressedFrameData.release();
                }
            }
        }
        super.write(ctx, msg, promise);
    }

    private boolean isCompressible(FrameData frameData) {
        int orderChannel = frameData.getOrderChannel();
        return frameData.getReliability().isReliable
                && frameData.getReliability().isOrdered
                && !frameData.getReliability().isSequenced
                && orderChannel >= 0
                && orderChannel < zstdInbound.length;
    }

    private void writeZstd(ChannelHandlerContext ctx, FrameData rawFrameData, ChannelPromise promise) {
        int rawLength = rawFrameData.getDataSize() - 1;
        if (rawLength < zstdThreshold || rawLength > MAX_ZSTD_DECOMPRESSED_SIZE) {
            outBytesRaw += rawLength;
            outBytesCompressed += rawLength;
            ctx.write(rawFrameData, promise);
            return;
        }

        ByteBuf data = rawFrameData.createData().skipBytes(1);
        byte[] compressed;
        try {
            byte[] input = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), input);
            compressed = ZstdCompressionUtil.compress(input, zstdLevel);
        } catch (RuntimeException e) {
            rawFrameData.release();
            throw e;
        } finally {
            data.release();
        }

        outBytesRaw += rawLength;
        if (compressed.length + 4 >= rawLength) {
            outBytesCompressed += rawLength;
            ctx.write(rawFrameData, promise);
            return;
        }

        ByteBuf out = null;
        FrameData compressedFrameData = null;
        try {
            out = ctx.alloc().buffer(compressed.length + 4, compressed.length + 4);
            out.writeInt(rawLength);
            out.writeBytes(compressed);
            outBytesCompressed += out.readableBytes();

            compressedFrameData = FrameData.create(ctx.alloc(), zstdPacketId, out);
            compressedFrameData.setReliability(rawFrameData.getReliability());
            compressedFrameData.setOrderChannel(rawFrameData.getOrderChannel());
            ctx.write(compressedFrameData, promise);
            compressedFrameData = null;
        } finally {
            rawFrameData.release();
            if (out != null) out.release();
            if (compressedFrameData != null) compressedFrameData.release();
        }
    }

    public long getInBytesCompressed() {
        return inBytesCompressed;
    }

    public long getInBytesRaw() {
        return inBytesRaw;
    }

    public long getOutBytesCompressed() {
        return outBytesCompressed;
    }

    public long getOutBytesRaw() {
        return outBytesRaw;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isZstdActive() {
        return zstdActive;
    }

    public String getCompressionAlgorithm() {
        if (zstdActive) return "zstd";
        if (active) return "zlib";
        return "none";
    }

    private ScheduledFuture<?> future;

    public void handlerAdded(ChannelHandlerContext ctx) {
        this.future = ctx.channel().eventLoop().scheduleAtFixedRate(this::tickMetrics, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (future != null) future.cancel(false);
        for (int i = 0; i < 8; i++) {
            if (inflaters[i] != null) inflaters[i].end();
            if (deflaters[i] != null) deflaters[i].end();
        }
    }

    private long lastInBytesCompressed;
    private long lastInBytesRaw;
    private long lastOutBytesRaw;
    private long lastOutBytesCompressed;
    private final DescriptiveStatistics inCompressionRatioStats = new DescriptiveStatistics(16);
    private final DescriptiveStatistics outCompressionRatioStats = new DescriptiveStatistics(16);
    private volatile double inCompressionRatio;
    private volatile double outCompressionRatio;

    private void tickMetrics() {
        long deltaInBytesCompressed = this.inBytesCompressed - this.lastInBytesCompressed;
        long deltaInBytesRaw = this.inBytesRaw - this.lastInBytesRaw;
        long deltaOutBytesCompressed = this.outBytesCompressed - this.lastOutBytesCompressed;
        long deltaOutBytesRaw = this.outBytesRaw - this.lastOutBytesRaw;

        if (deltaInBytesRaw != 0) {
            double currentInCompressionRatio = deltaInBytesCompressed / (double) deltaInBytesRaw;
            inCompressionRatioStats.addValue(currentInCompressionRatio);
        }

        if (deltaOutBytesRaw != 0) {
            double currentOutCompressionRatio = deltaOutBytesCompressed / (double) deltaOutBytesRaw;
            outCompressionRatioStats.addValue(currentOutCompressionRatio);
        }

        this.inCompressionRatio = inCompressionRatioStats.getMean();
        this.outCompressionRatio = outCompressionRatioStats.getMean();

        this.lastInBytesCompressed = this.inBytesCompressed;
        this.lastInBytesRaw = this.inBytesRaw;
        this.lastOutBytesCompressed = this.outBytesCompressed;
        this.lastOutBytesRaw = this.outBytesRaw;


    }

    public double getInCompressionRatio() {
        return inCompressionRatio;
    }

    public double getOutCompressionRatio() {
        return outCompressionRatio;
    }
}
