package io.wdsj.hybridfix.mixin.command;

import io.wdsj.hybridfix.command.CommandHybridFix;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleCommandMap.class)
public abstract class SimpleCommandMapMixin {

    @Shadow(remap = false) public abstract boolean register(String fallbackPrefix, Command command);

    @Inject(
            method = "setDefaultCommands",
            at = @At("HEAD"),
            remap = false
    )
    public void registerHybridFixCommand(CallbackInfo ci) {
        CommandHybridFix command = new CommandHybridFix("hybridfix");
        register("hybridfix", command);
    }
}
