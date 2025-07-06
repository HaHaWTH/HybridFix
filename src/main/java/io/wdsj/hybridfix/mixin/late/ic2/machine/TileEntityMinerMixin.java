package io.wdsj.hybridfix.mixin.late.ic2.machine;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(TileEntityMiner.class)
public abstract class TileEntityMinerMixin extends TileEntityElectricMachine {
    public TileEntityMinerMixin(int maxEnergy, int tier) {
        super(maxEnergy, tier);
    }

    @Inject(
            method = "canMine",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void canMine(BlockPos target, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock().hasTileEntity(state)) { // Skip TEs to prevent dupe bugs
            cir.setReturnValue(false);
            return;
        }

        FakePlayer dummy = Objects.requireNonNull(HybridFixFakePlayer.get(this.world, this.pos, "ic2-TileEntityMiner").get());
        if (EntityUtils.callBlockBreakEventForPlayer(this.world, target, dummy)) {
            cir.setReturnValue(false);
        }
    }
}
