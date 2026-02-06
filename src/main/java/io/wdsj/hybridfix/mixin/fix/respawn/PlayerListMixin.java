package io.wdsj.hybridfix.mixin.fix.respawn;

import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;copyFrom(Lnet/minecraft/entity/player/EntityPlayerMP;Z)V",
                    shift = At.Shift.AFTER,
                    remap = true
            ),
            remap = false
    )
    public void afterCopy(EntityPlayerMP player, int dimensionId, boolean conqueredEnd, Location loc, boolean avoidSuffocation, CallbackInfoReturnable<EntityPlayerMP> cir) {
        if (Settings.fixCapabilityReset) {
            FakePlayer dummyPlayer = Objects.requireNonNull(HybridFixFakePlayer.getPlayerCopy(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(player.dimension), player.getPosition(), player).get());
            dummyPlayer.copyFrom(player, conqueredEnd);
            //ForgeEventFactory.onPlayerClone(dummyPlayer, player, !conqueredEnd); // Fire another event for the fake player
            CapabilityDispatcher newCapability = ((EntityCapabilityAccessor) (Entity) dummyPlayer).getCapabilities();
            ((EntityCapabilityAccessor) (Entity) player).setCapabilities(newCapability); // Copy the re-gathered CapabilityDispatcher to the actual player
        }
    }
}
