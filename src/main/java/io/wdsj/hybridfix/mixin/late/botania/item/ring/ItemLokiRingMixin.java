package io.wdsj.hybridfix.mixin.late.botania.item.ring;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.common.item.relic.ItemLokiRing;

/**
 * @see <a href="https://bug.mcmod.cn/item/20181223.html">bugs.mcmod.cn</a>
 */
@Mixin(value = ItemLokiRing.class)
public abstract class ItemLokiRingMixin {

    /*
     * Botania is dumb, why use Item#onItemUse(...) instead of ItemStack#onItemUse(...)
     */
    @Redirect(
            method = "onPlayerInteract",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/Item;onItemUse(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;",
                    remap = true
            ),
            allow = 1
    )
    private static EnumActionResult useItemStackPlace(Item instance, EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, @Local(argsOnly = true) PlayerInteractEvent.RightClickBlock event) {
        return event.getItemStack().onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}
