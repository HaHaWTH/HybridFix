package io.wdsj.hybridfix.mixin.early_for_late.storage_drawers.itemstack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface ItemStackCapabilityAccessor {
    @Accessor(value = "capabilities", remap = false)
    CapabilityDispatcher getCapabilities();
}
