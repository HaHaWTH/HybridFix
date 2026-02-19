package io.wdsj.hybridfix.mixin.late.ic2.machine;

import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import io.wdsj.hybridfix.util.fake_player.HybridFixFakePlayer;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(TileEntityAdvMiner.class)
public abstract class TileEntityAdvMinerMixin extends TileEntityElectricMachine {
    public TileEntityAdvMinerMixin(int maxEnergy, int tier) {
        super(maxEnergy, tier);
    }

    @Inject(
            method = "canMine",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void canMine(BlockPos target, Block block, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (block.hasTileEntity(state)) { // DO NOT MINE TEs
            cir.setReturnValue(false);
            return;
        }

        FakePlayer dummy = Objects.requireNonNull(HybridFixFakePlayer.get(this.world, this.pos, "[ic2-TileEntityAdvMiner]").get());
        if (EntityUtils.callBlockBreakEventForPlayer(this.world, target, dummy)) {
            cir.setReturnValue(false);
        }
    }
}
