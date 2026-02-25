package io.wdsj.hybridfix.mixin.fix.mod_compat.jeid;

import io.wdsj.hybridfix.util.reflection.FluentReflect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.spongepowered.asm.mixin.*;

import java.lang.invoke.MethodHandle;

@Mixin(value = CraftWorld.class, remap = false)
public abstract class CraftWorldMixin {
    @Shadow
    @Final
    private WorldServer world;
    @Unique
    private static MethodHandle mh_getIntBiomeArray;

    /**
     * @author Creeam
     * @reason JEID
     */
    @Overwrite
    public void setBiome(int x, int z, Biome bio) throws Throwable {
        if (mh_getIntBiomeArray == null) {
            mh_getIntBiomeArray = FluentReflect.fromClass(Chunk.class)
                    .name("getIntBiomeArray")
                    .returnType(int[].class)
                    .virtualMethodHandle(); // delayed init for JEID mixin injection
        }
        net.minecraft.world.biome.Biome bb = CraftBlock.biomeToBiomeBase(bio);
        if (this.world.isBlockLoaded(new BlockPos(x, 0, z))) {
            Chunk chunk = this.world.getChunk(new BlockPos(x, 0, z));
            //noinspection ConstantConditions
            if (chunk != null) {
                int[] biomevals = (int[]) mh_getIntBiomeArray.invoke(chunk);
                biomevals[(z & 15) << 4 | x & 15] = net.minecraft.world.biome.Biome.REGISTRY.getIDForObject(bb);
                chunk.markDirty();
            }
        }
    }
}
