package io.wdsj.hybridfix.mixin.late.botania.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.item.ItemManaGun;

@Mixin(ItemManaGun.class)
public abstract class ItemManaGunMixin {
    /*
     * Botania, the best mod ever
     */
    @WrapOperation(
            method = "getBurst",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lvazkii/botania/common/entity/EntityManaBurst;"
            ),
            remap = false
    )
    private EntityManaBurst getBurst(EntityPlayer player, EnumHand hand, Operation<EntityManaBurst> original) {
        EntityManaBurst burst = original.call(player, hand);
        burst.thrower = player;
        return burst;
    }
}
