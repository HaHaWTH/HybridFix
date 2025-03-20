package io.wdsj.hybridfix.util.entity;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityUtils {
    private EntityUtils() {
    }

    public static boolean canDestroyBlock(World world, BlockPos pos, Entity entity) {
        return canDestroyBlock(world, pos, world.getBlockState(pos), entity);
    }

    public static boolean canDestroyBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        org.bukkit.entity.Entity bEntity = ((IEntityGetter) entity).getBukkitEntity();
        Block block = ((IWorldGetter) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        // noinspection deprecation
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(bEntity, block, Material.AIR, (byte) 0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        float hardness = state.getBlockHardness(world, pos);
        return hardness >= 0.0F && !state.getBlock().isAir(state, world, pos) && state.getBlock().canEntityDestroy(state, world, pos, entity) && (!(entity instanceof EntityLivingBase) || ForgeEventFactory.onEntityDestroyBlock((EntityLivingBase) entity, pos, state));
    }
}