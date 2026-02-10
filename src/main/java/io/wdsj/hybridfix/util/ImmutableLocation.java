package io.wdsj.hybridfix.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * A location that cannot be modified.
 */
@SuppressWarnings("unused")
public class ImmutableLocation extends Location {
    protected ImmutableLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    protected ImmutableLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    @Override
    public void setWorld(World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setX(double x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setY(double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setZ(double z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setYaw(float yaw) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPitch(float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location multiply(double m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location zero() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location subtract(Location vec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location subtract(double x, double y, double z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location subtract(Vector vec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location add(Location vec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location add(Vector vec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location add(double x, double y, double z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location setDirection(Vector vector) {
        throw new UnsupportedOperationException();
    }

    public static ImmutableLocation immutable(Location location) {
        return new ImmutableLocation(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location mutable() {
        return new Location(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    public static ImmutableLocation create(World world, double x, double y, double z) {
        return new ImmutableLocation(world, x, y, z);
    }

    public static ImmutableLocation create(World world, double x, double y, double z, float yaw, float pitch) {
        return new ImmutableLocation(world, x, y, z, yaw, pitch);
    }
}
