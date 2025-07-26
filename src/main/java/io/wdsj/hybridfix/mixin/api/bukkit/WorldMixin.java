package io.wdsj.hybridfix.mixin.api.bukkit;

import io.wdsj.hybridfix.duck.api.bukkit.IWorldInvoker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = World.class, remap = false)
public interface WorldMixin extends IWorldInvoker {
    @Shadow
    Collection<Entity> getNearbyEntities(Location location, double x, double y, double z);

    // Paper start
    @Override
    @Unique(silent = true)
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, radius, radius, radius);
    }

    @Override
    @Unique(silent = true)
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xzRadius, yRadius, xzRadius);
    }

    @Override
    @Unique(silent = true)
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xRadius, yRadius, zRadius);
    }

    @Override
    @Unique(silent = true)
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius, Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, radius, radius, radius, predicate);
    }

    @Override
    @Unique(silent = true)
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius, Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Override
    @Unique(silent = true)
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius, Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    @Override
    @Unique(silent = true)
    default Collection<Player> getNearbyPlayers(Location loc, double radius) {
        return getNearbyEntitiesByType(Player.class, loc, radius, radius, radius);
    }

    @Override
    @Unique(silent = true)
    default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(Player.class, loc, xzRadius, yRadius, xzRadius);
    }

    @Override
    @Unique(silent = true)
    default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(Player.class, loc, xRadius, yRadius, zRadius);
    }

    @Override
    @Unique(silent = true)
    default Collection<Player> getNearbyPlayers(Location loc, double radius, Predicate<Player> predicate) {
        return getNearbyEntitiesByType(Player.class, loc, radius, radius, radius, predicate);
    }

    @Override
    @Unique(silent = true)
    default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius, Predicate<Player> predicate) {
        return getNearbyEntitiesByType(Player.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Override
    @Unique(silent = true)
    default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius, Predicate<Player> predicate) {
        return getNearbyEntitiesByType(Player.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    @Override
    @Unique(silent = true)
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius) {
        return getNearbyEntitiesByType(clazz, loc, radius, radius, radius, null);
    }

    @Override
    @Unique(silent = true)
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, null);
    }

    @Override
    @Unique(silent = true)
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(clazz, loc, xRadius, yRadius, zRadius, null);
    }

    @Override
    @Unique(silent = true)
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius, Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, loc, radius, radius, radius, predicate);
    }

    @Override
    @Unique(silent = true)
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius, Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Override
    @Unique(silent = true)
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends Entity> clazz, Location loc, double xRadius, double yRadius, double zRadius, Predicate<T> predicate) {
        if (clazz == null) {
            clazz = Entity.class;
        }
        List<T> nearby = new ArrayList<>();
        for (Entity bukkitEntity : getNearbyEntities(loc, xRadius, yRadius, zRadius)) {
            //noinspection unchecked
            if (clazz.isAssignableFrom(bukkitEntity.getClass()) && (predicate == null || predicate.test((T) bukkitEntity))) {
                //noinspection unchecked
                nearby.add((T) bukkitEntity);
            }
        }
        return nearby;
    }
    // Paper end
}