package network.ycc.raknet.server.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.util.AttributeKey;
import io.wdsj.hybridfix.HybridFix;

import java.util.Map;

public class ChannelUtil {

    static void applyChannelParameters(Channel channel, RakNetServerChannel.ChannelParameters parameters) {
        setChannelOptions(channel, parameters.childOptions);
        for (Map.Entry<AttributeKey<?>, Object> e: parameters.childAttrs) {
            channel.attr((AttributeKey<Object>) e.getKey()).set(e.getValue());
        }
    }

    static void setChannelOptions(
            Channel channel, Map.Entry<ChannelOption<?>, Object>[] options) {
        for (Map.Entry<ChannelOption<?>, Object> e: options) {
            setChannelOption(channel, e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static void setChannelOption(
            Channel channel, ChannelOption<?> option, Object value) {
        try {
            if (!channel.config().setOption((ChannelOption<Object>) option, value)) {
                HybridFix.LOGGER.info("Unknown channel option '{}' for channel '{}'", option, channel);
            }
        } catch (Throwable t) {
            HybridFix.LOGGER.warn("Failed to set channel option '{}' with value '{}' for channel '{}'", option, value, channel, t);
        }
    }

}
