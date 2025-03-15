package io.wdsj.hybridfix.mixin.late.applied_energistics_2.spatial;

import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.util.WorldCoord;
import appeng.tile.spatial.TileSpatialIOPort;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import io.wdsj.hybridfix.api.bukkit.event.applied_energistics_2.spatial.SpatialPylonTransferEvent;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileSpatialIOPort.class)
public abstract class TileSpatialIOPortMixin {
    @WrapOperation(
            method = "call(Lnet/minecraft/world/World;)Ljava/lang/Void;",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/implementations/items/ISpatialStorageCell;doSpatialTransition(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lappeng/api/util/WorldCoord;Lappeng/api/util/WorldCoord;I)Lappeng/api/implementations/TransitionResult;"
            ),
            remap = false
    )
    public TransitionResult wrapSpatialTransition(ISpatialStorageCell instance, ItemStack itemStack, World world, WorldCoord minCoord, WorldCoord maxCoord, int id, Operation<TransitionResult> original, @Cancellable CallbackInfoReturnable<Void> cir) {
        org.bukkit.World bWorld = ((IWorldGetter) world).getWorld();
        Location min = new Location(bWorld, minCoord.x, minCoord.y, minCoord.z);
        Location max = new Location(bWorld, maxCoord.x, maxCoord.y, maxCoord.z);
        SpatialPylonTransferEvent event = new SpatialPylonTransferEvent(bWorld, min, max);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
            return new TransitionResult(false, 0.0D);
        }
        return original.call(instance, itemStack, world, minCoord, maxCoord, id);
    }
}