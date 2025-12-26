package io.wdsj.hybridfix.mixin.base.patch.forge.universal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.regex.Matcher;
import java.util.zip.ZipEntry;

@SuppressWarnings("LocalMayUseName")
@Mixin(value = JarDiscoverer.class, remap = false)
public class JarDiscovererMixin {
    @WrapOperation(
            method = "findClassesASM",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/regex/Matcher;matches()Z"
            ),
            require = 0
    )
    private boolean ignoreClasses(Matcher instance, Operation<Boolean> original, @Local(ordinal = 0) ZipEntry ze) {
        return original.call(instance) && hybridFix$isValidClass(ze);
    }

    @Unique
    private static boolean hybridFix$isValidClass(ZipEntry ze) {
        String name = ze.getName();
        if (name.equals("module-info.class") || name.endsWith("/module-info.class")) {
            return false;
        }
        return !name.startsWith("META-INF/versions/");
    }
}