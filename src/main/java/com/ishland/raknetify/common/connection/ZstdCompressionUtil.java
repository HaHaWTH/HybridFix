package com.ishland.raknetify.common.connection;

import com.github.luben.zstd.Zstd;
import io.wdsj.hybridfix.HybridFix;

final class ZstdCompressionUtil {

    private static final boolean AVAILABLE = probeAvailability();

    private ZstdCompressionUtil() {
    }

    static boolean isAvailable() {
        return AVAILABLE;
    }

    private static boolean probeAvailability() {
        try {
            return Zstd.compressBound(1L) > 0L;
        } catch (LinkageError e) {
            HybridFix.LOGGER.warn("Raknetify: zstd-jni native library is unavailable; using streaming zlib", e);
            return false;
        } catch (RuntimeException e) {
            HybridFix.LOGGER.warn("Raknetify: zstd-jni initialization failed; using streaming zlib", e);
            return false;
        }
    }

    static byte[] compress(byte[] input, int level) {
        long bound = Zstd.compressBound(input.length);
        if (bound <= 0L || bound > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid zstd compression bound: " + bound);
        }

        byte[] output = new byte[(int) bound];
        long result = Zstd.compressByteArray(output, 0, output.length, input, 0, input.length, level);
        checkResult(result, "compression");

        int compressedLength = (int) result;
        if (compressedLength == output.length) {
            return output;
        }
        byte[] trimmed = new byte[compressedLength];
        System.arraycopy(output, 0, trimmed, 0, compressedLength);
        return trimmed;
    }

    static byte[] decompress(byte[] input, int decompressedLength) {
        byte[] output = new byte[decompressedLength];
        long result = Zstd.decompressByteArray(output, 0, output.length, input, 0, input.length);
        checkResult(result, "decompression");
        if (result != decompressedLength) {
            throw new IllegalArgumentException("Zstd decompressed length mismatch: expected "
                    + decompressedLength + ", got " + result);
        }
        return output;
    }

    private static void checkResult(long result, String operation) {
        if (Zstd.isError(result)) {
            throw new IllegalArgumentException("Zstd " + operation + " failed: " + Zstd.getErrorName(result));
        }
    }
}
