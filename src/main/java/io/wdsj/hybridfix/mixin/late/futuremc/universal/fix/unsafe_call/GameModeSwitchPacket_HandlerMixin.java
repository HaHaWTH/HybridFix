package io.wdsj.hybridfix.mixin.late.futuremc.universal.fix.unsafe_call;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.util.TickThread;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thedarkcolour.futuremc.network.GameModeSwitchPacket;

@Mixin(GameModeSwitchPacket.Handler.class)
public abstract class GameModeSwitchPacket_HandlerMixin {
    @WrapOperation(
            method = "onMessage(Lthedarkcolour/futuremc/network/GameModeSwitchPacket;Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;)Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;setGameType(Lnet/minecraft/world/GameType;)V",
                    remap = true
            ),
            remap = false
    )
    private void fixUnsafeCall(EntityPlayerMP instance, GameType gameType, Operation<Void> original) {
        TickThread.ensureRunningOnMain(() -> original.call(instance, gameType));
    }
}
