package io.wdsj.hybridfix.mixin.fix.error_recovery.player_ticking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.api.bukkit.HybridFixBukkitApi;
import io.wdsj.hybridfix.config.Settings;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WorldServer.class, priority = 50000)
public abstract class WorldServerMixin {
    @Unique
    private static final boolean hybridFix$noPermRequirement = Settings.errorRecoverySettings.messagePermission.trim().isEmpty();
    @WrapOperation(
            method = "tickPlayers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;updateEntity(Lnet/minecraft/entity/Entity;)V"
            ),
            require = 0
    )
    private void guardTickPlayer(WorldServer instance, Entity entity, Operation<Void> original) {
        try {
            original.call(instance, entity);
        } catch (Throwable t) {
            CrashReport crashreport = CrashReport.makeCrashReport(t, "Ticking player");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
            entity.addEntityCrashInfo(crashreportcategory);
            FMLLog.log.fatal("{}", crashreport.getCompleteReport());
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP serverPlayer = (EntityPlayerMP) entity;
                BlockPos pos = entity.getPosition();
                final String msg = String.format("Player %s threw exception at %s:%s,%s,%s", serverPlayer.getName(), instance.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ());
                if (serverPlayer.connection != null) {
                    serverPlayer.connection.disconnect(new TextComponentString(msg));
                }
                if (Settings.errorRecoverySettings.broadcastMessage) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (HybridFixBukkitApi.getApi().isFakePlayer(player)) continue;
                        if (hybridFix$noPermRequirement || player.hasPermission(Settings.errorRecoverySettings.messagePermission)) {
                            player.sendMessage(msg);
                        }
                    }
                }
            }
        }
    }
}
