package io.wdsj.hybridfix.mixin.fix.packet_limiter.drop_item;

import io.wdsj.hybridfix.config.Settings;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {

    @Dynamic
    @ModifyConstant(
            method = "processPlayerDigging",
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/EntityPlayerMP;dropItem(Z)Lnet/minecraft/entity/item/EntityItem;",
                            ordinal = 0
                    )
            ),
            constant = @Constant(intValue = 20),
            require = 1
    )
    private int processPlayerDigging(int original) {
        return Settings.packetSettings.maxDroppedItemsPerTick;
    }
}
