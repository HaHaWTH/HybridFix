package io.wdsj.hybridfix.mixin.late.aether_legacy.universal.fix.unsafe_call;

import com.gildedgames.the_aether.networking.packets.PacketOpenContainer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.wdsj.hybridfix.util.TickThread;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PacketOpenContainer.class)
public abstract class PacketOpenContainerMixin {
    @WrapMethod(
            method = "handleServer(Lcom/gildedgames/the_aether/networking/packets/PacketOpenContainer;Lnet/minecraft/entity/player/EntityPlayer;)V",
            remap = false
    )
    public void handleServer(PacketOpenContainer message, EntityPlayer player, Operation<Void> original) {
        if (TickThread.isTickThread()) {
            original.call(message, player);
            return;
        }
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(
                () -> original.call(message, player)
        );
    }
}
