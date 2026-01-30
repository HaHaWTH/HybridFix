package io.wdsj.hybridfix.mixin.perf.core.math;

import io.wdsj.hybridfix.util.Vec3iHasher;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Vec3i.class, priority = 999)
public abstract class Vec3iMixin {
    // @formatter:off
    @Shadow @Final private int x;
    @Shadow @Final private int y;
    @Shadow @Final private int z;
    // @formatter:on

    /**
     * @author Creeam
     * @reason Optimize Vec3i hashing
     */
    @Overwrite
    public int hashCode() {
        return Vec3iHasher.hash(this.x, this.y, this.z);
    }
}
