package io.wdsj.hybridfix.mixin.late.voxelmap.client.fix.teleport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mamiyaotaru.voxelmap.gui.GuiWaypoints;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiWaypoints.class)
public abstract class GuiWaypointsMixin extends GuiScreenMinimap {
    @WrapOperation(
            method = "actionPerformed",
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
