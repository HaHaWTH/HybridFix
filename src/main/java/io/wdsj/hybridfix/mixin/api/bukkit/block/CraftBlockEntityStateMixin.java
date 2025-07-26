package io.wdsj.hybridfix.mixin.api.bukkit.block;

import io.wdsj.hybridfix.state.block.BlockEntitySnapshotState;
import net.minecraft.tileentity.TileEntity;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockEntityState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftBlockEntityState.class, remap = false)
public abstract class CraftBlockEntityStateMixin {
    @Inject(
            method = "createSnapshot",
            at = @At("HEAD"),
            cancellable = true
    )
    public <T extends TileEntity> void createSnapshot(T tileEntity, CallbackInfoReturnable<T> cir) {
        if (!BlockEntitySnapshotState.ENABLE_SNAPSHOT && tileEntity != null) {
            cir.setReturnValue(tileEntity);
        }
    }
}
