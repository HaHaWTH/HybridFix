package io.wdsj.hybridfix.duck.api.bukkit;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Duck interface for {@link org.bukkit.World}.
 * You can cast to this from a {@link org.bukkit.World} instance.
 */
@SuppressWarnings("unused")
public interface IWorldInvoker {
    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc    Center location
     * @param radius Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc      Center location
     * @param xzRadius X/Z Radius
     * @param yRadius  Y Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc     Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc       Center location
     * @param radius    X Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection
     */
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius, Predicate<LivingEntity> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc       Center location
     * @param xzRadius  X/Z Radius
     * @param yRadius   Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection
     */
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius, Predicate<LivingEntity> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc       Center location
     * @param xRadius   X Radius
     * @param yRadius   Y Radius
     * @param zRadius   Z radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius, Predicate<LivingEntity> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc    Center location
     * @param radius X/Y/Z Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    default Collection<Player> getNearbyPlayers(Location loc, double radius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc      Center location
     * @param xzRadius X/Z Radius
     * @param yRadius  Y Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc     Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc       Center location
     * @param radius    X/Y/Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default Collection<Player> getNearbyPlayers(Location loc, double radius, Predicate<Player> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc       Center location
     * @param xzRadius  X/Z Radius
     * @param yRadius   Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius, Predicate<Player> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     *
     * @param loc       Center location
     * @param xRadius   X Radius
     * @param yRadius   Y Radius
     * @param zRadius   Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius, Predicate<Player> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     *
     * @param clazz  Type to filter by
     * @param loc    Center location
     * @param radius X/Y/Z radius to search within
     * @param <T>    the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     *
     * @param clazz    Type to filter by
     * @param loc      Center location
     * @param xzRadius X/Z radius to search within
     * @param yRadius  Y radius to search within
     * @param <T>      the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     *
     * @param clazz   Type to filter by
     * @param loc     Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param <T>     the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xRadius, double yRadius, double zRadius) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     *
     * @param clazz     Type to filter by
     * @param loc       Center location
     * @param radius    X/Y/Z radius to search within
     * @param predicate a predicate used to filter results
     * @param <T>       the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius, Predicate<T> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     *
     * @param clazz     Type to filter by
     * @param loc       Center location
     * @param xzRadius  X/Z radius to search within
     * @param yRadius   Y radius to search within
     * @param predicate a predicate used to filter results
     * @param <T>       the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius, Predicate<T> predicate) {
        throw new AssertionError("Not Implemented");
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     *
     * @param clazz     Type to filter by
     * @param loc       Center location
     * @param xRadius   X Radius
     * @param yRadius   Y Radius
     * @param zRadius   Z Radius
     * @param predicate a predicate used to filter results
     * @param <T>       the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends Entity> clazz, Location loc, double xRadius, double yRadius, double zRadius, Predicate<T> predicate) {
        throw new AssertionError("Not Implemented");
    }
}
