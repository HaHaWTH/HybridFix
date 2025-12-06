package io.wdsj.hybridfix.mixin.late.mekanism.tile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityDigitalMiner.class)
public class TileEntityDigitalMinerMixin {

    @WrapOperation(
            method = "canMine",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/api/Coord4D;getBlockState(Lnet/minecraft/world/IBlockAccess;)Lnet/minecraft/block/state/IBlockState;",
                    remap = false
            ),
            remap = false
    )
    public IBlockState canMine(Coord4D instance, IBlockAccess world, Operation<IBlockState> original, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        IBlockState val = original.call(instance, world);
        if (val.getBlock().hasTileEntity(val)) {
            cir.setReturnValue(false);
        }
        return val;
    }
}
