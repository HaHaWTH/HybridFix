package io.wdsj.hybridfix.handler.voxelmap;

import java.util.Objects;

public class SerializedResidence {
    public final String name;
    public final String owner;
    public final int minX;
    public final int minZ;
    public final int maxX;
    public final int maxZ;

    public final int colorHash;

    public SerializedResidence(String name, String owner, int minX, int minZ, int maxX, int maxZ) {
        this.name = name;
        this.owner = owner;
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.colorHash = (name.hashCode() & 0x00FFFFFF) | 0x4D000000;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SerializedResidence)) return false;
        SerializedResidence other = (SerializedResidence) obj;
        return name.equals(other.name) && owner.equals(other.owner) && minX == other.minX && minZ == other.minZ && maxX == other.maxX && maxZ == other.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner, minX, minZ, maxX, maxZ);
    }
}
