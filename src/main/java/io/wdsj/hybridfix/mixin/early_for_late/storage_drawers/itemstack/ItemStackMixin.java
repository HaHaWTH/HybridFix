package io.wdsj.hybridfix.mixin.early_for_late.storage_drawers.itemstack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.state.late.storage_drawers.SkipCapState;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(
            method = "<init>(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;forgeInit()V",
                    remap = false
            )
    )
    public void forgeInit(ItemStack instance, Operation<Void> original) {
        if (SkipCapState.SKIP_CAP_INIT.get()) {
            return;
        }
        original.call(instance);
    }
}
