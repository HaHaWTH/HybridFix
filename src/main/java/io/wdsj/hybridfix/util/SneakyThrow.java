package io.wdsj.hybridfix.util;

public class SneakyThrow {
    private SneakyThrow() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneaky(Throwable t) throws T {
        throw (T) t;
    }
}
