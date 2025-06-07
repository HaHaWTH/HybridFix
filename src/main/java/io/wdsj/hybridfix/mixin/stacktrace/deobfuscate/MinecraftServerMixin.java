package io.wdsj.hybridfix.mixin.stacktrace.deobfuscate;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.util.ModCompatUtils;
import io.wdsj.hybridfix.util.SneakyThrow;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Dynamic
    @WrapOperation(
            method = "loadAllWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/v1_12_R1/CraftServer;enablePlugins(Lorg/bukkit/plugin/PluginLoadOrder;)V",
                    remap = false
            )
    )
    public void deobfuscate(CraftServer instance, PluginLoadOrder loadOrder, Operation<Void> original) {
        try {
            original.call(instance, loadOrder);
        } catch (Throwable t) {
            if (ModCompatUtils.isCensoredASMInstalled()) {
                ModCompatUtils.censoredASM_deobfuscateThrowable(t);
            }
            SneakyThrow.throw0(t);
        }
    }
}
