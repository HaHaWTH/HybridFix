package io.wdsj.hybridfix.mixin.late.ic2.machine;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.HybridFixFakePlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.block.BlockBreakEvent;
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

        World bWorld = ((IWorldGetter) this.getWorld()).getWorld();
        Block bBlock = bWorld.getBlockAt(target.getX(), target.getY(), target.getZ());
        CraftPlayer dummy = (CraftPlayer) ((IEntityGetter) Objects.requireNonNull(HybridFixFakePlayer.get(this.world, this.pos, "ic2-TileEntityMiner").get())).getBukkitEntity();
        BlockBreakEvent event = new BlockBreakEvent(bBlock, dummy);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
