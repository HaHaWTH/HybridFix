package io.wdsj.hybridfix.mixin.fix.error_recovery;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @Inject(
            method = "init",
            at = @At(
                    value = "TAIL"
            )
    )
    public void onInit(CallbackInfoReturnable<Boolean> cir) {
        if (!Settings.errorRecoverySettings.messagePermission.trim().isEmpty()) {
            try {
                Permission permission = new Permission(Settings.errorRecoverySettings.messagePermission, null, PermissionDefault.OP);
                DefaultPermissions.registerPermission(permission);
            } catch (Exception e) {
                HybridFix.LOGGER.error("Failed to register error recovery permission", e);
            }
        }
    }
}
