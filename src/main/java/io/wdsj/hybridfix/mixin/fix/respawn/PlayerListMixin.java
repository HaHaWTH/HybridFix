package io.wdsj.hybridfix.mixin.fix.respawn;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.logic.respawn.CBRespawnFixLogic;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

// Common injection logic for most hybrid server implementations, except Mohist
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Dynamic("craftbukkit")
    @Inject(
            method = "moveToWorld(Lnet/minecraft/entity/player/EntityPlayerMP;IZLorg/bukkit/Location;Z)Lnet/minecraft/entity/player/EntityPlayerMP;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftPlayer;getWorld()Lorg/bukkit/World;"
            ),
            remap = false
    )
    public void onMoveToWorld(EntityPlayerMP playerIn, int i, boolean b, Location loc, boolean b1, CallbackInfoReturnable<EntityPlayerMP> cir, @Share("newCap") LocalRef<CapabilityDispatcher> newCap) {
        if (Settings.simulateVanillaRespawn) CBRespawnFixLogic.simulateVanillaRespawn(playerIn);
        if (Settings.fixCapabilityReset) {
            newCap.set(ForgeEventFactory.gatherCapabilities(playerIn));
        }
    }

    @Dynamic("craftbukkit")
    @Inject(
            method = "moveToWorld(Lnet/minecraft/entity/player/EntityPlayerMP;IZLorg/bukkit/Location;Z)Lnet/minecraft/entity/player/EntityPlayerMP;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;copyFrom(Lnet/minecraft/entity/player/EntityPlayerMP;Z)V",
                    shift = At.Shift.AFTER,
                    remap = true
            ),
            remap = false
    )
    public void afterCopy(EntityPlayerMP player, int i, boolean b, Location loc, boolean b1, CallbackInfoReturnable<EntityPlayerMP> cir, @Share("newCap") LocalRef<CapabilityDispatcher> newCap) {
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
