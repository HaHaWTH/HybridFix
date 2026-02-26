package io.wdsj.hybridfix.handler.voxelmap;

import java.awt.Color;
import java.util.Objects;

public class SerializedResidence {
    public final String name;
    public final String owner;
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public final int colorHash;

    public SerializedResidence(String name, String owner, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.name = name;
        this.owner = owner;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        float hue = (float) (Math.abs(owner.hashCode() % 360) / 360.0);
        long variationSeed = Objects.hash(name, minX, minZ);
        float saturation = 0.5f + (float) (Math.abs(variationSeed % 30) / 100.0);
        float brightness = 0.6f + (float) (Math.abs((variationSeed >> 4) % 30) / 100.0);
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);

        this.colorHash = (rgb & 0x00FFFFFF) | 0x4D000000;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SerializedResidence)) return false;
        SerializedResidence other = (SerializedResidence) obj;
        return minX == other.minX && minZ == other.minZ &&
                maxX == other.maxX && maxZ == other.maxZ &&
                minY == other.minY && maxY == other.maxY &&
                name.equals(other.name) && owner.equals(other.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner, minX, minY, minZ, maxX, maxY, maxZ);
    }
}