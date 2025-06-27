package io.wdsj.hybridfix.mixin.late.ic2.client.audio;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ic2.core.audio.AudioManagerClient;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Field;

/**
 * Use direct call instead of expensive reflections.
 */
@Mixin(AudioManagerClient.class)
public abstract class AudioManagerClientMixin {

    @WrapOperation(
            method = "valid",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/reflect/Field;getBoolean(Ljava/lang/Object;)Z"
            ),
            remap = false
    )
    public boolean wrapGetBoolean(Field field, Object instance, Operation<Boolean> original) {
        return ((SoundManager) instance).loaded;
    }
}
