package io.wdsj.hybridfix.mixin.raknetify.client;

import com.mojang.authlib.GameProfile;
import io.wdsj.hybridfix.mixin.raknetify.client.accessor.WorldClientAccessor;
import io.wdsj.hybridfix.raknetify.ClientFoodStats112;
import io.wdsj.hybridfix.raknetify.RaknetifyConnectionUtil112;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    @Shadow protected FoodStats foodStats;

    protected EntityPlayerMixin(World world) {
        super(world);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lcom/mojang/authlib/GameProfile;)V", at = @At("RETURN"), remap = false)
    private void raknetify$replaceClientFoodStats(World world, GameProfile profile, CallbackInfo ci) {
        if (world instanceof WorldClient) {
            NetHandlerPlayClient connection = ((WorldClientAccessor) world).raknetify$getConnection();
            if (connection == null || !RaknetifyConnectionUtil112.isRakNet(connection.getNetworkManager().channel())) {
                return;
            }
            this.foodStats = ClientFoodStats112.from(this.foodStats);
        }
    }
}
