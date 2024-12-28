package io.wdsj.hybridfix.mixin.fix.capability.mohist;

import io.wdsj.hybridfix.mixin.fix.capability.EntityCapabilityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Mohist inlines method body into recreatePlayerEntity from CraftBukkit moveToWorld, causes common mixin won't work
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
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityEvent.EntityConstructing(playerIn));
        ((EntityCapabilityAccessor) (Entity) playerIn).setCapabilities(net.minecraftforge.event.ForgeEventFactory.gatherCapabilities(playerIn));
    }
}
