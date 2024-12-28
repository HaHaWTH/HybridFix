package io.wdsj.hybridfix.mixin.perf.timings.v1;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.TimingsCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TimingsCommand.class)
public abstract class TimingsCommandMixin {
    @Inject(
            method = "executeSpigotTimings",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void disableNotice(CommandSender sender, String[] args, CallbackInfo ci) {
        sender.sendMessage("§c[HybridFix] Timings has performance issues and has been disabled, use spark instead.");
        ci.cancel();
    }
}
