package io.wdsj.hybridfix.mixin.command;

import io.wdsj.hybridfix.command.CommandHybridFix;
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
        Permission permission = new Permission(CommandHybridFix.ERASE_ENTITY_PERMISSION, null, PermissionDefault.OP);
        DefaultPermissions.registerPermission(permission);
    }
}
