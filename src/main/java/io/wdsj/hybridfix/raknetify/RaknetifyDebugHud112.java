package io.wdsj.hybridfix.raknetify;

import com.ishland.raknetify.common.connection.MetricsSynchronizationHandler;
import com.ishland.raknetify.common.connection.MultiChannelingStreamingCompression;
import com.ishland.raknetify.common.connection.SimpleMetricsLogger;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import network.ycc.raknet.RakNet;

import java.util.function.Consumer;

public final class RaknetifyDebugHud112 {
    private RaknetifyDebugHud112() {
    }

    public static void append(Consumer<String> output) {
        NetHandlerPlayClient handler = Minecraft.getMinecraft().getConnection();
        Channel channel = handler == null ? null : handler.getNetworkManager().channel();
        if (channel == null || !(channel.config() instanceof RakNet.Config)) {
            output.accept("[Raknetify] A: false");
            return;
        }
        RakNet.Config config = (RakNet.Config) channel.config();

        if (config.getMetrics() instanceof SimpleMetricsLogger) {
            SimpleMetricsLogger logger = (SimpleMetricsLogger) config.getMetrics();
            output.accept(String.format("[Raknetify] A: true, MTU: %d, RTT: %.2f/%.2fms",
                    config.getMTU(), logger.getMeasureRTTns() / 1_000_000.0,
                    logger.getMeasureRTTnsStdDev() / 1_000_000.0));
            MetricsSynchronizationHandler remote = logger.getMetricsSynchronizationHandler();
            if (remote != null && remote.isRemoteSupported()) {
                output.accept(String.format("[Raknetify] C: BUF: %.2fMB; S: BUF: %.2fMB",
                        logger.getCurrentQueuedBytes() / 1048576.0, remote.getQueuedBytes() / 1048576.0));
            } else {
                output.accept(String.format("[Raknetify] C: BUF: %.2fMB",
                        logger.getCurrentQueuedBytes() / 1048576.0));
            }
            output.accept(String.format("[Raknetify] C: I: %s, O: %s",
                    logger.getMeasureTrafficInFormatted(), logger.getMeasureTrafficOutFormatted()));
            output.accept(String.format("[Raknetify] C: ERR: %.4f%%, %d tx, %d rx, BST: %d",
                    logger.getMeasureErrorRate() * 100.0, logger.getMeasureTX(), logger.getMeasureRX(),
                    logger.getMeasureBurstTokens() + config.getDefaultPendingFrameSets()));
            if (remote != null && remote.isRemoteSupported()) {
                output.accept(String.format("[Raknetify] S: ERR: %.4f%%, %d tx, %d rx, BST: %d",
                        remote.getErrorRate() * 100.0, remote.getTX(), remote.getRX(), remote.getBurst()));
            }
        } else {
            output.accept(String.format("[Raknetify] A: true, MTU: %d", config.getMTU()));
        }

        MultiChannelingStreamingCompression compression =
                channel.pipeline().get(MultiChannelingStreamingCompression.class);
        if (compression != null && compression.isActive()) {
            output.accept(String.format("[Raknetify] Compression: %s, CRatio: I: %.2f%%, O: %.2f%%",
                    compression.getCompressionAlgorithm(),
                    compression.getInCompressionRatio() * 100.0,
                    compression.getOutCompressionRatio() * 100.0));
        }
    }
}
