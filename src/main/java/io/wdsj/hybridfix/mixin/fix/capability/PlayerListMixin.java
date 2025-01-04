package io.wdsj.hybridfix.mixin.fix.capability;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Common injection logic for most hybrid server implementations, except Mohist
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
            method = "moveToWorld(Lnet/minecraft/entity/player/EntityPlayerMP;IZLorg/bukkit/Location;Z)Lnet/minecraft/entity/player/EntityPlayerMP;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftPlayer;getWorld()Lorg/bukkit/World;"
            ),
            remap = false
    )
    public void onMoveToWorld(EntityPlayerMP playerIn, int i, boolean b, Location loc, boolean b1, CallbackInfoReturnable<EntityPlayerMP> cir) {
        CBRespawnFixLogic.fixRespawn(playerIn);
    }
}
