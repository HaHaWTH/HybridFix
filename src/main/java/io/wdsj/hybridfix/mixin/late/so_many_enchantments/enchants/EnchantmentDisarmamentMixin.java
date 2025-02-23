package io.wdsj.hybridfix.mixin.late.so_many_enchantments.enchants;

import com.shultrea.rin.enchantments.base.EnchantmentBase;
import com.shultrea.rin.enchantments.weapon.EnchantmentDisarmament;
import com.shultrea.rin.mixin.vanilla.IEntityLivingMixin;
import com.shultrea.rin.util.compat.CompatUtil;
import com.shultrea.rin.util.compat.RLCombatCompat;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EnchantmentDisarmament.class, remap = false)
public abstract class EnchantmentDisarmamentMixin extends EnchantmentBase {

    public EnchantmentDisarmamentMixin(String name, Rarity rarity, EntityEquipmentSlot... slots) {
        super(name, rarity, slots);
    }

    @Inject(
            method = "onLivingAttackEvent",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void onLivingAttackEvent(LivingAttackEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Unique
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void hybridFix$onLivingDamageEvent(LivingDamageEvent event) {
        if (!this.isEnabled()) return;
        if (!EnchantmentBase.isDamageSourceAllowed(event.getSource())) return;
        if (CompatUtil.isRLCombatLoaded() && !RLCombatCompat.isAttackEntityFromStrong()) return;
        if (event.getAmount() <= 1.0F) return;
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if (attacker == null) return;
        EntityLivingBase victim = event.getEntityLiving();
        if (victim == null) return;
        // if (victim.world.isRemote) return;
        ItemStack stack = attacker.getHeldItemMainhand();
        if (stack.isEmpty()) return;

        int level = EnchantmentHelper.getEnchantmentLevel(this, stack);
        if (level > 0) {
            if (attacker.getRNG().nextFloat() < 0.02F * (float) level) {
                if (!victim.getHeldItemMainhand().isEmpty()) {
                    if (victim instanceof EntityLiving && attacker.getRNG().nextFloat() >= ((IEntityLivingMixin) victim).getInventoryHandsDropChances()[0])
                        return;
                    victim.entityDropItem(victim.getHeldItemMainhand(), 0.5F);
                    victim.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                } else if (!victim.getHeldItemOffhand().isEmpty()) {
                    if (victim instanceof EntityLiving && attacker.getRNG().nextFloat() >= ((IEntityLivingMixin) victim).getInventoryHandsDropChances()[1])
                        return;
                    victim.entityDropItem(victim.getHeldItemOffhand(), 0.5F);
                    victim.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
                }
            }
        }
    }
}
