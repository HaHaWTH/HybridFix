package io.wdsj.hybridfix.util.entity;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Nullable
    public static EntityLivingBase rayTraceEntity(@NotNull Entity originEntity, double maxDistance) {
        World world = originEntity.world;
        Vec3d start = originEntity.getPositionEyes(1.0F);

        Vec3d lookVec = originEntity.getLook(1.0F);

        Vec3d end = start.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);

        RayTraceResult blockHit = world.rayTraceBlocks(start, end, false, true, true);
        double blockDistance = (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK)
                ? start.distanceTo(blockHit.hitVec)
                : maxDistance;

        AxisAlignedBB aabb = originEntity.getEntityBoundingBox()
                .expand(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance)
                .grow(1.0D, 1.0D, 1.0D);

        List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb, entity -> entity != originEntity);

        EntityLivingBase closestEntity = null;
        double closestDistance = blockDistance;

        for (Entity entity : entities) {
            float borderSize = entity.getCollisionBorderSize();
            AxisAlignedBB entityBB = entity.getEntityBoundingBox()
                    .grow(borderSize, borderSize, borderSize);

            RayTraceResult intercept = entityBB.calculateIntercept(start, end);
            if (intercept != null) {
                double distance = start.distanceTo(intercept.hitVec);
                if (distance < closestDistance) {
                    closestEntity = (EntityLivingBase) entity;
                    closestDistance = distance;
                }
            }
        }

        return closestEntity;
    }
}