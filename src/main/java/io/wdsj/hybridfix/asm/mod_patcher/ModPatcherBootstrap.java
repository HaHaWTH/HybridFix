package io.wdsj.hybridfix.asm.mod_patcher;

import io.wdsj.hybridfix.asm.IBytecodePatcher;
import net.minecraft.launchwrapper.IClassTransformer;

@SuppressWarnings("unused")
public final class ModPatcherBootstrap implements IClassTransformer {
    @Override
    public byte[] transform(String untransformedName, String transformedName, byte[] bytes) {
        if (shouldSkipClass(transformedName)) {
            return bytes;
        }
        return ModPatcherManager.INSTANCE.processTransform(untransformedName, transformedName, bytes);
    }

    private static boolean shouldSkipClass(String className) {
        return className.startsWith("io.wdsj.hybridfix.") || IBytecodePatcher.isCommonPackage(className);
    }
}
