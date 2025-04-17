package io.wdsj.hybridfix.mixin.api.antixray;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.imanity.antixray.sdk.AntiXrayAdapter;
import dev.imanity.antixray.sdk.AntiXraySDK;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class WorldMixin {
    @WrapOperation(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"
            )
    )
    public IBlockState onSetBlockState(Chunk instance, BlockPos pos, IBlockState state, Operation<IBlockState> original) {
        IBlockState val = original.call(instance, pos, state);
        AntiXrayAdapter adapter = AntiXraySDK.getAdapter();
        if (adapter != null) {
            adapter.callBlockChange(((IWorldGetter) this).getWorld(), pos.getX(), pos.getY(), pos.getZ(), CraftMagicNumbers.getMaterial(state.getBlock()));
        }
        return val;
    }
}
