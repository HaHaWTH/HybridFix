package io.wdsj.hybridfix.mixin.raknetify.client.accessor;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldClient.class)
public interface WorldClientAccessor {
    @Accessor("connection")
    NetHandlerPlayClient raknetify$getConnection();
}
