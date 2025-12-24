package io.wdsj.hybridfix.mixin.base.patch.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import org.spongepowered.asm.mixin.Mixin;
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
            )
    )
    private boolean ignoreMultiReleaseClasses(Matcher instance, Operation<Boolean> original, @Local(ordinal = 0) ZipEntry ze) {
        if (ze.getName().startsWith("META-INF/versions")) {
            return false;
        }
        return original.call(instance);
    }
}