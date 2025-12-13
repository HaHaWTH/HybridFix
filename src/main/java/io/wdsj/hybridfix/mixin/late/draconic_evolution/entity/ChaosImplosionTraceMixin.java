package io.wdsj.hybridfix.mixin.late.draconic_evolution.entity;

import com.brandon3055.draconicevolution.entity.ProcessChaosImplosion;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Batch process and fire Bukkit EntityExplodeEvent
 */
@Mixin(ProcessChaosImplosion.ChaosImplosionTrace.class)
public abstract class ChaosImplosionTraceMixin {
    @Shadow(remap = false) private World world;
    @Unique
    private int hybridFix$startX;
    @Unique
    private int hybridFix$startY;
    @Unique
    private int hybridFix$startZ;
    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    public void onInit(ProcessChaosImplosion this$0, World world, int x, int y, int z, float power, Random random, CallbackInfo ci) {
        this.hybridFix$startX = x;
        this.hybridFix$startY = y;
        this.hybridFix$startZ = z;
    }
    @WrapOperation(
            method = "updateProcess",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z",
                    remap = true
            ),
            remap = false
    )
    private boolean onSetBlockToAir(World instance, BlockPos pos, Operation<Boolean> original, @Share("blockList") LocalRef<List<BlockPos>> blockList) {
        if (blockList.get() == null) blockList.set(new ObjectArrayList<>());
        blockList.get().add(pos);
        return false;
    }

    @WrapOperation(
            method = "updateProcess",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
                    remap = true
            ),
            remap = false
    )
    private boolean onAttackEntityFrom(Entity instance, DamageSource source, float amount, Operation<Boolean> original, @Share("entityMap") LocalRef<Map<Entity, Float>> entityMap) {
        if (entityMap.get() == null) entityMap.set(new Object2FloatOpenHashMap<>());
        entityMap.get().merge(instance, amount, Float::sum);
        return false;
    }

    @Inject(
            method = "updateProcess",
            at = @At("TAIL"),
            remap = false
    )
    private void onUpdateProcess(CallbackInfo ci, @Share("blockList") LocalRef<List<BlockPos>> blockList, @Share("entityMap") LocalRef<Map<Entity, Float>> entityMap) {
        List<BlockPos> list = blockList.get();
        Map<Entity, Float> map = entityMap.get();
        if (list != null) {
            BlockPos start = new BlockPos(this.hybridFix$startX, this.hybridFix$startY, this.hybridFix$startZ);
            EntityPlayerMP dummy = Objects.requireNonNull(HybridFixFakePlayer.get(this.world, start, "[draconicevolution-ChaosImplosionTrace]").get());
            if (EntityUtils.callBukkitEntityExplodeEvent(this.world, start, list, dummy)) {
                if (map != null && Settings.removeEntityDamageAndVelocityOnCancel) {
                    map.clear();
                }
            } else {
                for (BlockPos pos : list) {
                    this.world.setBlockToAir(pos);
                }
            }
        }
        if (map != null) {
            for (Map.Entry<Entity, Float> entry : map.entrySet()) {
                Entity entity = entry.getKey();
                float amount = entry.getValue();
                entity.attackEntityFrom(ProcessChaosImplosion.chaosImplosion, amount);
            }
        }
    }
}
