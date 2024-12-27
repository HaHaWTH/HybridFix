package io.wdsj.hybridfix.mixin.fix.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityEvent.EntityConstructing(playerIn));
        ((EntityCapabilityAccessor) (Entity) playerIn).setCapabilities(net.minecraftforge.event.ForgeEventFactory.gatherCapabilities(playerIn));
    }
}
