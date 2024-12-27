package io.wdsj.hybridfix.mixin.fix.capability;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityCapabilityAccessor {
    @Accessor(value = "capabilities", remap = false)
    void setCapabilities(CapabilityDispatcher capabilities);
}
