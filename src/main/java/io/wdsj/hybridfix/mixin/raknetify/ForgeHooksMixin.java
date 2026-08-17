package io.wdsj.hybridfix.mixin.raknetify;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksMixin {

    /**
     * Forge normally sends a speculative AIR block update before posting the
     * break event. If the event is already known to be cancelled (for example,
     * a creative player holding a sword), it immediately follows that packet
     * with the real block state. RakNet can flush those writes separately and
     * expose the transient AIR state for a frame. Keep the event and correction,
     * but omit the known-invalid speculative update on RakNet connections.
     */
    @WrapOperation(
            method = "onBlockBreakEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 0,
                    remap = true
            ),
            remap = false
    )
    private static void hybridfix$skipPreCancelledRakNetAirUpdate(NetHandlerPlayServer connection, Packet<?> packet, Operation<Void> original) {
        if (RaknetifyConnectionUtil112.isRakNet(connection.netManager.channel()) && packet instanceof SPacketBlockChange) {
            SPacketBlockChange packetBlockChange = (SPacketBlockChange) packet;
            if (packetBlockChange.blockState == Blocks.AIR.getDefaultState()) return;
        }
        original.call(connection, packet);
    }
}
