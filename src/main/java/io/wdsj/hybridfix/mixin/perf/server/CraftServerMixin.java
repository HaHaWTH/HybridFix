package io.wdsj.hybridfix.mixin.perf.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(CraftServer.class)
public abstract class CraftServerMixin {

    @Mutable
    @Shadow(remap = false) @Final private Map<String, World> worlds;

    @Inject(
            method = "<init>",
            at = @At("RETURN"),
            remap = false
    )
    public void onInit(CallbackInfo ci) {
        this.worlds = new Object2ObjectLinkedOpenHashMap<>();
    }

    @Inject(
            method = "getWorlds",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void onGetWorlds(CallbackInfoReturnable<List<World>> cir) {
        cir.setReturnValue(new ObjectArrayList<>(this.worlds.values()));
    }
}
