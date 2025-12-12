package io.wdsj.hybridfix.mixin.api.antixray;

import dev.imanity.antixray.sdk.AntiXrayAdapter;
import dev.imanity.antixray.sdk.AntiXraySDK;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(
            method = "onBlockClicked",
            at = @At(
                    value = "TAIL"
            )
    )
    public void onBlockLeftClicked(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        AntiXrayAdapter adapter = AntiXraySDK.getAdapter();
        if (adapter != null) {
            World bWorld = player.world.getWorld();
            Player bPlayer = (Player) player.getBukkitEntity();
            adapter.callPlayerLeftClickBlock(bWorld, bPlayer, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
