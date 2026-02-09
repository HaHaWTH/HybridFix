package io.wdsj.hybridfix.util;

import net.minecraftforge.fml.common.FMLCommonHandler;

import static io.wdsj.hybridfix.HybridFix.LOGGER;

public final class TickThread {
    public static void ensureTickThread(final String reason) {
        if (!isTickThread()) {
            LOGGER.error("Thread failed main thread check: {}, context={}", reason, new Throwable());
            throw new IllegalStateException(reason);
        }
    }

    public static boolean isTickThread() {
        return Thread.currentThread() == FMLCommonHandler.instance().getMinecraftServerInstance().getServerThread();
    }
}
