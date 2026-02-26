package io.wdsj.hybridfix.mixin.perf.server.universal;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = EntityTracker.class, priority = 500)
public abstract class EntityTrackerMixin {
    @Shadow @Final @Mutable
    private Set<EntityTrackerEntry> entries;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(WorldServer theWorldIn, CallbackInfo ci) {
        this.entries = new ObjectLinkedOpenHashSet<>();
    }
}
