package io.wdsj.hybridfix.mixin.fix.chunk_system;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftWorld.class)
public abstract class CraftWorldMixin {
    /**
     * Preventing unloading Forge forced chunks from Bukkit side.
     */
    @WrapOperation(
            method = "unloadChunk0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/ChunkProviderServer;unloadChunk(Lnet/minecraft/world/chunk/Chunk;Z)Z"
            ),
            remap = false
    )
    public boolean unloadChunk0(ChunkProviderServer instance, Chunk chunk, boolean b, Operation<Boolean> original) {
        World world = ((CraftWorld) (Object) this).getHandle();
        if (world.getPersistentChunks().containsKey(chunk.getPos())) {
            return false;
        }
        return original.call(instance, chunk, b);
    }
}
