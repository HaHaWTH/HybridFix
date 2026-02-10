package io.wdsj.hybridfix.mixin.late.aether_legacy.universal.entity;

import com.gildedgames.the_aether.entities.passive.mountable.EntitySwet;
import com.gildedgames.the_aether.entities.util.EntityMountable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

// Reference: https://github.com/The-Aether-Team/The-Aether-Archived/issues/186
// https://github.com/The-Aether-Team/The-Aether-Archived/commit/099591bc4c9777c2366fd82d1e54ac08163eaa45
@Mixin(EntitySwet.class)
public abstract class EntitySwetMixin extends EntityMountable {
    public EntitySwetMixin(World world) {
        super(world);
    }

    @Override
    public boolean canBeSteered() {
        return false;
    }

    @Override
    public void onMountedJump(float par1, float par2) {
    }
}
