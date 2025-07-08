package io.wdsj.hybridfix.util.entity;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityUtils {
    private EntityUtils() {
    }

    /**
     * If target mod has no safety checks, use this.
     */
    public static boolean canDestroyBlock(World world, BlockPos pos, Entity entity) {
        return canDestroyBlock(world, pos, world.getBlockState(pos), entity);
    }

    /**
     * If target mod has no safety checks, use this.
     */
    public static boolean canDestroyBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityPlayerMP) {
            if (callBlockBreakEventForPlayer(world, pos, state, (EntityPlayerMP) entity)) {
                return false;
            }
        } else {
            if (callBlockBreakEventForEntity(world, pos, state, entity)) {
                return false;
            }
        }
        float hardness = state.getBlockHardness(world, pos);
        return hardness >= 0.0F && !state.getBlock().isAir(state, world, pos) && state.getBlock().canEntityDestroy(state, world, pos, entity);
    }

    /**
     * @return true if the event was cancelled, false otherwise
     */
    public static boolean callBlockBreakEventForPlayer(World world, BlockPos pos, EntityPlayerMP player) {
        return callBlockBreakEventForPlayer(world, pos, world.getBlockState(pos), player);
    }

    /**
     * @return true if the event was cancelled, false otherwise
     */
    public static boolean callBlockBreakEventForPlayer(World world, BlockPos pos, IBlockState state, EntityPlayerMP player) {
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    /**
     * @return true if the event was cancelled, false otherwise
     */
    public static boolean callBlockBreakEventForEntity(World world, BlockPos pos, IBlockState state, Entity entity) {
        org.bukkit.entity.Entity bEntity = ((IEntityGetter) entity).getBukkitEntity();
        Block block = ((IWorldGetter) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if (entity instanceof EntityLivingBase && !ForgeEventFactory.onEntityDestroyBlock((EntityLivingBase) entity, pos, state)) {
            return true;
        }
        if (!ForgeEventFactory.getMobGriefingEvent(world, entity)) {
            return true;
        }
        // noinspection deprecation
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(bEntity, block, Material.AIR, (byte) 0);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    public static boolean callBukkitEntityExplodeEvent(World world, BlockPos start, List<BlockPos> affectedBlocks, Entity entity) {
        org.bukkit.World bWorld = ((IWorldGetter) world).getWorld();
        org.bukkit.entity.Entity bEntity = ((IEntityGetter) entity).getBukkitEntity();
        List<Block> blockList = new ObjectArrayList<>(affectedBlocks.size());
        for (int i1 = affectedBlocks.size() - 1; i1 >= 0; i1--) {
            BlockPos cpos = affectedBlocks.get(i1);
            Block bblock = bWorld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
            if (bblock.getType() != Material.AIR) {
                blockList.add(bblock);
            }
        }
        EntityExplodeEvent bukkitEvent = new EntityExplodeEvent(bEntity, new Location(bWorld, start.getX(), start.getY(), start.getZ()), blockList, 0.0F);
        Bukkit.getServer().getPluginManager().callEvent(bukkitEvent);
        boolean isCancelled = bukkitEvent.isCancelled();
        if (!isCancelled) {
            affectedBlocks.clear();
            for (Block bblock : blockList) {
                BlockPos coords = new BlockPos(bblock.getX(), bblock.getY(), bblock.getZ());
                affectedBlocks.add(coords);
            }
        }
        return isCancelled;
    }

    @Nullable
    public static EntityLivingBase rayTraceLivingEntity(@NotNull Entity originEntity) {
        return rayTraceLivingEntity(originEntity, FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getEntityViewDistance());
    }

    @Nullable
    public static EntityLivingBase rayTraceLivingEntity(@NotNull Entity originEntity, double maxDistance) {
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

    @Nullable
    public static BlockPos rayTraceBlock(@NotNull Entity originEntity, double maxDistance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
        World world = originEntity.world;
        Vec3d start = originEntity.getPositionEyes(1.0F);

        Vec3d lookVec = originEntity.getLook(1.0F);

        Vec3d end = start.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);

        RayTraceResult blockHit = world.rayTraceBlocks(start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, true);
        if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }
        return null;
    }
}