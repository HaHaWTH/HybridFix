package io.wdsj.hybridfix.mixin.fix.respawn.mohist;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.mixin.fix.respawn.EntityCapabilityAccessor;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/*
 * Mohist inlines the method body into recreatePlayerEntity from CraftBukkit moveToWorld, causes common mixins no longer work
 * now we need to handle this manually.
 */
@Mixin(PlayerList.class)
@SuppressWarnings("all")
public abstract class PlayerListMixin {
    @Dynamic("craftbukkit")
    @Inject(
            method = "func_72368_a(Lnet/minecraft/entity/player/EntityPlayerMP;IZ)Lnet/minecraft/entity/player/EntityPlayerMP;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;copyFrom(Lnet/minecraft/entity/player/EntityPlayerMP;Z)V",
                    shift = At.Shift.AFTER,
                    remap = true
            ),
            remap = false
    )
    public void afterCopy(EntityPlayerMP player, int i, boolean b, CallbackInfoReturnable<EntityPlayerMP> cir) {
        if (Settings.fixCapabilityReset) {
            FakePlayer dummyPlayer = Objects.requireNonNull(HybridFixFakePlayer.getPlayerCopy(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(player.dimension), player.getPosition(), player).get());
            dummyPlayer.copyFrom(player, b);
            //ForgeEventFactory.onPlayerClone(dummyPlayer, player, !conqueredEnd); // Fire another event for the fake player
            CapabilityDispatcher newCapability = ((EntityCapabilityAccessor) (Entity) dummyPlayer).getCapabilities();
            ((EntityCapabilityAccessor) (Entity) player).setCapabilities(newCapability); // Copy the re-gathered CapabilityDispatcher to the actual player
        }
    }
}
