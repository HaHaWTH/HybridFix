package io.wdsj.hybridfix.mixin.api.bukkit;

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
public interface WorldMixin {
    @Shadow Collection<Entity> getNearbyEntities(Location location, double x, double y, double z);

    // Paper start
    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param radius Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, radius, radius, radius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xzRadius, yRadius, xzRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xRadius, yRadius, zRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param radius X Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection
     */
    @Unique(silent = true)
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius, Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, radius, radius, radius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection
     */
    @Unique(silent = true)
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius, Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius, Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(LivingEntity.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param radius X/Y/Z Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<Player> getNearbyPlayers(Location loc, double radius) {
        return getNearbyEntitiesByType(Player.class, loc, radius, radius, radius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(Player.class, loc, xzRadius, yRadius, xzRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @return the collection of players near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(Player.class, loc, xRadius, yRadius, zRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param radius X/Y/Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<Player> getNearbyPlayers(Location loc, double radius, Predicate<Player> predicate) {
        return getNearbyEntitiesByType(Player.class, loc, radius, radius, radius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius, Predicate<Player> predicate) {
        return getNearbyEntitiesByType(Player.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius, Predicate<Player> predicate) {
        return getNearbyEntitiesByType(Player.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param radius X/Y/Z radius to search within
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius) {
        return getNearbyEntitiesByType(clazz, loc, radius, radius, radius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xzRadius X/Z radius to search within
     * @param yRadius Y radius to search within
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(clazz, loc, xRadius, yRadius, zRadius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param radius X/Y/Z radius to search within
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius, Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, loc, radius, radius, radius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xzRadius X/Z radius to search within
     * @param yRadius Y radius to search within
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius, Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    @Unique(silent = true)
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends Entity> clazz, Location loc, double xRadius, double yRadius, double zRadius, Predicate<T> predicate) {
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
