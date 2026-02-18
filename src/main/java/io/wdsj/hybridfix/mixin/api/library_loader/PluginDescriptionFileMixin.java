package io.wdsj.hybridfix.mixin.api.library_loader;

import com.google.common.collect.ImmutableList;
import io.wdsj.hybridfix.duck.api.library_loader.IPluginDescriptionFile;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = PluginDescriptionFile.class, remap = false)
public abstract class PluginDescriptionFileMixin implements IPluginDescriptionFile {
    @Unique
    private List<String> libraries = ImmutableList.of();

    @Unique
    @NotNull
    @Override
    public List<String> getLibraries() {
        return libraries;
    }

    @Inject(
            method = "loadMap",
            at = @At("TAIL")
    )
    private void loadMap(Map<?, ?> map, CallbackInfo ci) throws InvalidDescriptionException {
        if (map.get("libraries") != null) {
            ImmutableList.Builder<String> contributorsBuilder = ImmutableList.builder();
            try {
                for (Object o : (Iterable<?>) map.get("libraries")) {
                    contributorsBuilder.add(o.toString());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "libraries are of wrong type");
            }
            libraries = contributorsBuilder.build();
        } else {
            libraries = ImmutableList.of();
        }
    }

    @Inject(
            method = "saveMap",
            at = @At("RETURN")
    )
    private void saveMap(CallbackInfoReturnable<Map<String, Object>> cir) {
        if (libraries != null) {
            cir.getReturnValue().put("libraries", libraries);
        }
    }
}
