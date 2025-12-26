package io.wdsj.hybridfix.mixin.fix.respawn;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
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
                    target = "Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftPlayer;getWorld()Lorg/bukkit/World;",
                    ordinal = 0
            ),
            remap = false
    )
    public void onMoveToWorld(EntityPlayerMP playerIn, int dimensionId, boolean conqueredEnd, Location loc, boolean avoidSuffocation, CallbackInfoReturnable<EntityPlayerMP> cir, @Share("newCap") LocalRef<CapabilityDispatcher> newCap) {
        if (Settings.fixCapabilityReset) {
            newCap.set(ForgeEventFactory.gatherCapabilities(playerIn)); // Prepare a new capability dispatcher
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
    public void afterCopy(EntityPlayerMP player, int dimensionId, boolean conqueredEnd, Location loc, boolean avoidSuffocation, CallbackInfoReturnable<EntityPlayerMP> cir, @Share("newCap") LocalRef<CapabilityDispatcher> newCap) {
        if (Settings.fixCapabilityReset) {
            FakePlayer dummyPlayer = Objects.requireNonNull(HybridFixFakePlayer.getPlayerCopy(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(player.dimension), new BlockPos(loc.getX(), loc.getY(), loc.getZ()), player).get());
            CapabilityDispatcher dispatcher = newCap.get();
            ((EntityCapabilityAccessor) (Entity) dummyPlayer).setCapabilities(dispatcher); // Set the fake player's capabilities to the new dispatcher
            dummyPlayer.copyFrom(player, conqueredEnd);
            //ForgeEventFactory.onPlayerClone(dummyPlayer, player, !conqueredEnd); // Fire another event for the fake player
            CapabilityDispatcher newCapability = ((EntityCapabilityAccessor) (Entity) dummyPlayer).getCapabilities();
            ((EntityCapabilityAccessor) (Entity) player).setCapabilities(newCapability); // Copy the re-gathered CapabilityDispatcher to the actual player
        }
    }
}
