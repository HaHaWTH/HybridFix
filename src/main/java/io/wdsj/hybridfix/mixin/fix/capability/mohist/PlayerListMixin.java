package io.wdsj.hybridfix.mixin.fix.capability.mohist;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.logic.respawn.CBRespawnFixLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Mohist inlines method body into recreatePlayerEntity from CraftBukkit moveToWorld, causes common mixin don't work
 * now we need to handle this manually.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
            method = "func_72368_a(Lnet/minecraft/entity/player/EntityPlayerMP;IZ)Lnet/minecraft/entity/player/EntityPlayerMP;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftPlayer;getWorld()Lorg/bukkit/World;"
            ),
            remap = false
    )
    public void func(EntityPlayerMP playerIn, int i, boolean b, CallbackInfoReturnable<EntityPlayerMP> cir) {
        if (Settings.simulateVanillaRespawn) CBRespawnFixLogic.simulateVanillaRespawn(playerIn);
        if (Settings.fixCapabilityReset) CBRespawnFixLogic.regatherCapabilities(playerIn);
    }
}
