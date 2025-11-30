package io.wdsj.hybridfix.mixin.fix.error_recovery.broadcast;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.api.bukkit.HybridFixBukkitApi;
import io.wdsj.hybridfix.config.Settings;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
    @Unique
    private static final boolean hybridFix$noPermRequirement = Settings.errorRecoverySettings.messagePermission.trim().isEmpty();
    @Inject(
            method = "updateEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Object;)V",
                    remap = false
            ),
            require = 0
    )
    private void notify(CallbackInfo ci) {
        if (!Settings.errorRecoverySettings.broadcastMessage) return;
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (HybridFixBukkitApi.getApi().isFakePlayer(player)) continue;
                if (hybridFix$noPermRequirement || player.hasPermission(Settings.errorRecoverySettings.messagePermission)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Settings.errorRecoverySettings.message.replace("%error_point%", "updateEntities")));
                }
            }
        } catch (Throwable t) {
            HybridFix.LOGGER.error("Error recovery failed to send messages: ", t);
        }
    }
}
