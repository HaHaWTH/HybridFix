package io.wdsj.hybridfix.raknetify;

import com.ishland.raknetify.common.util.NetworkInterfaceListener;

/** Early, side-neutral initialization required by the imported Raknetify runtime. */
public final class RaknetifyBootstrap112 {
    private static volatile boolean initialized;

    private RaknetifyBootstrap112() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        System.setProperty("raknetserver.maxPacketLoss", Integer.toString(Integer.MAX_VALUE));
        NetworkInterfaceListener.init();
        RaknetifyMultiChannel112.getPacketChannelOverride(null, true);
        initialized = true;
    }
}
