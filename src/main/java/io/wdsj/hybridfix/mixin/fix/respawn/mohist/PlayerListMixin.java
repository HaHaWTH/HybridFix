package io.wdsj.hybridfix.mixin.fix.respawn.mohist;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.logic.respawn.CBRespawnFixLogic;
import io.wdsj.hybridfix.mixin.fix.respawn.EntityCapabilityAccessor;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/*
 * Mohist inlines method body into recreatePlayerEntity from CraftBukkit moveToWorld, causes common mixins no longer work
 * now we need to handle this manually.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Dynamic("mohist")
    @Inject(
            method = "func_72368_a(Lnet/minecraft/entity/player/EntityPlayerMP;IZ)Lnet/minecraft/entity/player/EntityPlayerMP;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftPlayer;getWorld()Lorg/bukkit/World;"
            ),
            remap = false
    )
    public void func(EntityPlayerMP playerIn, int i, boolean b, CallbackInfoReturnable<EntityPlayerMP> cir, @Share("newCap") LocalRef<CapabilityDispatcher> newCap) {
        if (Settings.simulateVanillaRespawn) CBRespawnFixLogic.simulateVanillaRespawn(playerIn);
        if (Settings.fixCapabilityReset) {
            newCap.set(ForgeEventFactory.gatherCapabilities(playerIn));
        }
    }

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
    public void afterCopy(EntityPlayerMP player, int i, boolean b, CallbackInfoReturnable<EntityPlayerMP> cir, @Share("newCap") LocalRef<CapabilityDispatcher> newCap) {
        if (Settings.fixCapabilityReset) {
            FakePlayer dummy = Objects.requireNonNull(HybridFixFakePlayer.get(player.getEntityWorld(), player.getPosition()).get());
            CapabilityDispatcher dispatcher = newCap.get();
            ((EntityCapabilityAccessor) (Entity) dummy).setCapabilities(dispatcher);
            ForgeEventFactory.onPlayerClone(dummy, player, !b);
            CapabilityDispatcher newCapability = ((EntityCapabilityAccessor) (Entity) dummy).getCapabilities();
            ((EntityCapabilityAccessor) (Entity) player).setCapabilities(newCapability);
        }
    }
}
