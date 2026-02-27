package io.wdsj.hybridfix.mixin.late.voxelmap.teleport.fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiPersistentMap.class)
public abstract class GuiPersistentMapMixin {
    @WrapOperation(
            method = "popupAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;sendChatMessage(Ljava/lang/String;)V"
            )
    )
    public void fixTeleport(EntityPlayerSP instance, String message, Operation<Void> original) {
        if (message.startsWith("/tppos")) return;
        if (message.startsWith("/tp")) {
            message = message.replaceFirst("/tp", "/minecraft:tp");
        }
        original.call(instance, message);
    }
}
