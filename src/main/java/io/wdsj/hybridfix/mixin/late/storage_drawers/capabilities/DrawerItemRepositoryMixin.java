package io.wdsj.hybridfix.mixin.late.storage_drawers.capabilities;

import com.jaquadro.minecraft.storagedrawers.capabilities.DrawerItemRepository;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wdsj.hybridfix.mixin.early_for_late.storage_drawers.itemstack.ItemStackCapabilityAccessor;
import io.wdsj.hybridfix.state.late.storage_drawers.SkipCapState;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DrawerItemRepository.class, remap = false)
public abstract class DrawerItemRepositoryMixin {
    @Inject(
            method = "stackResult",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void stackResult(ItemStack stack, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getCount() == amount) {
            cir.setReturnValue(stack);
        }
    }
    /**
     * How this optimization works: Most of the time, the ItemStack passed to this method has no capabilities.
     * (nobody wants to insert a battery into the drawer, right?)
     * So we can safely skip the capability gathering process, this will improve performance drastically in large modpacks.
     */
    @WrapOperation(
            method = "stackResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;",
                    remap = true
            )
    )
    public ItemStack stackResult(ItemStack instance, Operation<ItemStack> original) {
        try {
            // noinspection ConstantConditions
            if (((ItemStackCapabilityAccessor) (Object) instance).getCapabilities() == null) {
                SkipCapState.SKIP_CAP_INIT = true;
            }
            return original.call(instance);
        } finally {
            SkipCapState.SKIP_CAP_INIT = false;
        }
    }
}
