package io.wdsj.hybridfix.handler.explosion;

import io.wdsj.hybridfix.api.forge.HybridFixForgeApi;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class ExplosionStartHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosionStart(ExplosionEvent.Start event) {
        Explosion explosion = event.getExplosion();
        if (!HybridFixForgeApi.getApi().isVanillaExplosionEventStart(event) || explosion.getClass() != Explosion.class) {
            Entity exploder = explosion.exploder;
            World bworld = event.getWorld().getWorld();
            Vec3d explosionPos = explosion.getPosition();
            Location location = new Location(bworld, explosionPos.x, explosionPos.y, explosionPos.z);
            boolean cancelled;
            final List<Block> bukkitBlocks = new ObjectArrayList<>(0); // empty list
            if (exploder != null) {
                EntityExplodeEvent bukkitEvent = new EntityExplodeEvent(exploder.getBukkitEntity(), location, bukkitBlocks, 1.0F / explosion.size);
                Bukkit.getServer().getPluginManager().callEvent(bukkitEvent);
                cancelled = bukkitEvent.isCancelled();
            } else {
                BlockExplodeEvent bukkitEvent = new BlockExplodeEvent(location.getBlock(), bukkitBlocks, 1.0F / explosion.size);
                Bukkit.getServer().getPluginManager().callEvent(bukkitEvent);
                cancelled = bukkitEvent.isCancelled();
            }
            if (cancelled) {
                event.setCanceled(true);
            }
        }
    }
}
