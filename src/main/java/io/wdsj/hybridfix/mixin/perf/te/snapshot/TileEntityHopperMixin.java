package io.wdsj.hybridfix.mixin.perf.te.snapshot;

import io.wdsj.hybridfix.state.block.BlockEntitySnapshotState;
import io.wdsj.hybridfix.util.TickThread;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityHopper.class)
public abstract class TileEntityHopperMixin {
    @Inject(
            method = "pullItemFromSlot",
            at = @At("HEAD")
    )
    private static void pullItemFromSlotHead(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction, CallbackInfoReturnable<Boolean> cir) {
        TickThread.ensureTickThread("cannot pull item off-main");
        BlockEntitySnapshotState.ENABLE_SNAPSHOT = false;
    }

    @Inject(
            method = "pullItemFromSlot",
            at = @At("RETURN")
    )
    private static void pullItemFromSlotReturn(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction, CallbackInfoReturnable<Boolean> cir) {
        BlockEntitySnapshotState.ENABLE_SNAPSHOT = true;
    }
}
