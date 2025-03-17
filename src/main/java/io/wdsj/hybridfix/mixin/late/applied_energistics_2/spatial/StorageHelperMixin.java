package io.wdsj.hybridfix.mixin.late.applied_energistics_2.spatial;

import appeng.spatial.StorageHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.config.Settings;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(StorageHelper.class)
public abstract class StorageHelperMixin {
    @Unique
    private static final Set<String> hybridFix$blacklist = Arrays.stream(Settings.modPatchSettings.spatialPylonEntityBlacklist)
            .map(String::toLowerCase)
            .collect(Collectors.toCollection(ObjectOpenHashSet::new));

    @WrapOperation(
            method = "swapRegions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;",
                    remap = true
            ),
            remap = false
    )
    public <T extends Entity> List<T> filterEntities(World instance, Class<? extends T> classEntity, AxisAlignedBB bb, Operation<List<T>> original) {
        return instance.getEntitiesWithinAABB(classEntity, bb, entity -> EntitySelectors.NOT_SPECTATING.apply(entity) && !hybridFix$isBanned(entity));
    }

    @Unique
    private boolean hybridFix$isBanned(Entity entity) {
        ResourceLocation rl = EntityList.getKey(entity);
        if (rl != null) {
            return Settings.modPatchSettings.invertSpatialPylonEntityBlacklist != hybridFix$blacklist.contains(rl.toString());
        }
        return false;
    }
}
