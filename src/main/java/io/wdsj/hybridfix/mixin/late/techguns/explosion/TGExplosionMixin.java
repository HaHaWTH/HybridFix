package io.wdsj.hybridfix.mixin.late.techguns.explosion;

import io.wdsj.hybridfix.api.forge.HybridFixForgeApi;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import techguns.damagesystem.TGExplosion;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mixin(value = TGExplosion.class, remap = false)
public abstract class TGExplosionMixin {
    @Shadow
    HashMap<BlockPos, Double> affectedBlockPositions;

    @Redirect(
            method = "doExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;onExplosionDetonate(Lnet/minecraft/world/World;Lnet/minecraft/world/Explosion;Ljava/util/List;D)V"
            )
    )
    public void onExplosionDetonate(World world, Explosion explosion, List<Entity> list, double diameter) {
        List<BlockPos> vanillaAffectedBlocks = explosion.getAffectedBlockPositions();
        explosion.clearAffectedBlockPositions();
        vanillaAffectedBlocks.addAll(this.affectedBlockPositions.keySet());

        ExplosionEvent.Detonate event = new ExplosionEvent.Detonate(world, explosion, list);
        HybridFixForgeApi.getApi().setVanillaExplosionDetonateEvent(event, false);
        MinecraftForge.EVENT_BUS.post(event);

        Set<BlockPos> finalAffectedBlocks = new ObjectOpenHashSet<>(explosion.getAffectedBlockPositions());
        this.affectedBlockPositions.entrySet().removeIf(entry ->
                !finalAffectedBlocks.contains(entry.getKey())
        );
    }
}
