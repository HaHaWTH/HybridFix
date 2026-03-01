package io.wdsj.hybridfix.util;

import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.concurrent.Executor;

import static io.wdsj.hybridfix.HybridFix.LOGGER;

@SuppressWarnings("unused")
public final class TickThread {
    private static final Executor MAIN_THREAD = r -> FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(r);
    public static void ensureTickThread(final String reason) {
        if (!isTickThread()) {
            LOGGER.error("Thread failed main thread check: {}, context={}", reason, new Throwable());
            throw new IllegalStateException(reason);
        }
    }

    public static void ensureRunningOnMain(Runnable runnable) {
        if (!isTickThread()) {
            mainThreadExecutor().execute(runnable);
        } else {
            runnable.run();
        }
    }

    public static boolean isTickThread() {
        return Thread.currentThread() == FMLCommonHandler.instance().getMinecraftServerInstance().getServerThread();
    }

    public static Executor mainThreadExecutor() {
        return MAIN_THREAD;
    }
}
