package io.wdsj.hybridfix.asm.mod_patcher;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

@SuppressWarnings("unused")
public final class ModPatcherBootstrap implements IClassTransformer {
    static {
        Launch.classLoader.addTransformerExclusion("io.wdsj.hybridfix");
    }
    @Override
    public byte[] transform(String untransformedName, String transformedName, byte[] bytes) {
        return ModPatcherManager.INSTANCE.processTransform(untransformedName, transformedName, bytes);
    }
}
