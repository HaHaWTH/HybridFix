package io.wdsj.hybridfix.util;

import org.bukkit.Location;
import org.bukkit.World;

public class ImmutableLocation extends Location {
    protected ImmutableLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
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

    public static ImmutableLocation fromLocation(Location location) {
        return new ImmutableLocation(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }
}
