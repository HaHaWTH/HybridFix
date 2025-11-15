package io.wdsj.hybridfix.mixin.fix.chunk_system;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftWorld.class)
public abstract class CraftWorldMixin {
    /**
     * Preventing unloading Forge forced chunks from Bukkit side.
     */
    @Inject(
            method = "unloadChunk0",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    public void unloadChunk0(int x, int z, boolean save, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) Chunk chunk) {
        if (chunk == null) return;
        World world = ((CraftWorld) (Object) this).getHandle();
        if (world.getPersistentChunks().containsKey(chunk.getPos())) {
            cir.setReturnValue(false);
        }
    }
}
