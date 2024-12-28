package io.wdsj.hybridfix.handler;

import com.google.common.collect.Lists;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.duck.bridge.explosion.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.explosion.IWorldGetter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class ExplosionHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        if (explosion.getClass() != Explosion.class) {
            Entity exploder = explosion.exploder;
            World bworld = ((IWorldGetter) event.getWorld()).getWorld();
            Vec3d explosionPos = explosion.getPosition();
            Location location = new Location(bworld, explosionPos.x, explosionPos.y, explosionPos.z);
            List<Block> bukkitBlocks;
            boolean cancelled;
            float yield;
            final List<Block> blockList = Lists.newArrayList();
            List<BlockPos> affectedBlockPositions = event.getAffectedBlocks();
            for (int i1 = affectedBlockPositions.size() - 1; i1 >= 0; i1--) {
                BlockPos cpos = affectedBlockPositions.get(i1);
                Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (bblock.getType() != Material.AIR) {
                    blockList.add(bblock);
                }
            }
            if (exploder != null) {
                EntityExplodeEvent bukkitEvent = new EntityExplodeEvent(((IEntityGetter) exploder).getBukkitEntity(), location , blockList, 1.0F / explosion.size);
                Bukkit.getServer().getPluginManager().callEvent(bukkitEvent);
                cancelled = bukkitEvent.isCancelled();
                bukkitBlocks = bukkitEvent.blockList();
                yield = bukkitEvent.getYield();
            } else {
                BlockExplodeEvent bukkitEvent = new BlockExplodeEvent(location.getBlock(), blockList, 1.0F / explosion.size);
                Bukkit.getServer().getPluginManager().callEvent(bukkitEvent);
                cancelled = bukkitEvent.isCancelled();
                bukkitBlocks = bukkitEvent.blockList();
                yield = bukkitEvent.getYield();
            }
            explosion.getAffectedBlockPositions().clear();

            if (cancelled) {
                if (Settings.removeEntityDamageAndVelocityOnCancel) event.getAffectedEntities().clear();
            } else {
                for (Block bblock : bukkitBlocks) {
                    BlockPos coords = new BlockPos(bblock.getX(), bblock.getY(), bblock.getZ());
                    explosion.getAffectedBlockPositions().add(coords);
                }
                explosion.size = yield * explosion.size;
            }
        }
    }
}
