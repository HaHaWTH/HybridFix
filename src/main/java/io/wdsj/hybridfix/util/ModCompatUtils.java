package io.wdsj.hybridfix.util;

import net.minecraftforge.fml.common.Loader;
import zone.rong.loliasm.api.StacktraceDeobfuscator;

public class ModCompatUtils {
    private static final boolean isCensoredASMInstalled = Loader.isModLoaded("loliasm");

    public static boolean isCensoredASMInstalled() {
        return isCensoredASMInstalled;
    }

    public static void censoredASM_deobfuscateThrowable(Throwable th) {
        if (!isCensoredASMInstalled()) throw new IllegalStateException("Censored ASM is not installed!");
        StacktraceDeobfuscator.deobfuscateThrowable(th);
    }
}
