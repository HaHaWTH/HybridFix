package io.wdsj.hybridfix.mixin.late.tconstruct.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.tools.common.network.TinkerStationTabPacket;

@Mixin(value = TinkerStationTabPacket.class, remap = false)
public abstract class TinkerStationTabPacketMixin {
    // @formatter:off
    @Shadow public int blockX;
    @Shadow public int blockY;
    @Shadow public int blockZ;
    // @formatter:on

    @Inject(
            method = "handleServerSafe",
            at = @At("HEAD"),
            cancellable = true
    )
    private void handleServerSafe(NetHandlerPlayServer netHandler, CallbackInfo ci) {
        EntityPlayerMP player = netHandler.player;
        if (player == null) return;
        BlockPos pos = new BlockPos(this.blockX, this.blockY, this.blockZ);
        if (!player.getEntityWorld().isBlockLoaded(pos)) {
            ci.cancel();
        }
    }
}
