package io.wdsj.hybridfix.mixin.late.witchery.infusion.symbol;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.HybridFixServer;
import io.wdsj.hybridfix.util.reflection.ReflectionChain;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.msrandom.witchery.infusion.symbol.AccioSymbolEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.invoke.MethodHandle;

/**
 * Early fire {@link PlayerPickupItemEvent} and {@link EntityPickupItemEvent} before Accio is applied.
 */
@SuppressWarnings({"deprecation", "LocalMayUseName"})
@Mixin(AccioSymbolEffect.class)
public abstract class AccioSymbolEffectMixin {
    /*
     * A helper method offered by CraftBukkit
     * Descriptor: public int canHold (net.minecraft.item.ItemStack)
     */
    @Unique
    private static final MethodHandle mh_InventoryPlayer_canHold;

    static {
        MethodHandle mh;
        try {
            mh = ReflectionChain.fromClass(InventoryPlayer.class)
                    .name("canHold")
                    .param(ItemStack.class)
                    .accessible(true)
                    .virtualMethodHandle();
        } catch (Throwable t) {
            mh = null;
            HybridFix.LOGGER.error("Failed to get InventoryPlayer#canHold, server will continue to run, but some features may not work.");
            HybridFixServer.createServerDump(t);
        }
        mh_InventoryPlayer_canHold = mh;
    }

    @WrapOperation(
            method = "onCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/item/EntityItem;setPosition(DDD)V",
                    remap = true
            ),
            remap = false
    )
    public void wrapSetPosition(EntityItem instance, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) EntityLivingBase caster) {
        try {
            Item item = (Item) instance.getBukkitEntity();
            if (caster instanceof EntityPlayerMP) {
                if (mh_InventoryPlayer_canHold != null) {
                    EntityPlayerMP player = (EntityPlayerMP) caster;
                    Player bPlayer = (Player) player.getBukkitEntity();
                    ItemStack itemStack = instance.getItem();
                    int remaining = itemStack.getCount() - (int) mh_InventoryPlayer_canHold.invokeExact(player.inventory, itemStack);
                    PlayerPickupItemEvent old = new PlayerPickupItemEvent(bPlayer, item, remaining);
                    Bukkit.getPluginManager().callEvent(old);
                    if (old.isCancelled()) return;
                    EntityPickupItemEvent event = new EntityPickupItemEvent(bPlayer, item, remaining);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;
                }
            } else {
                Entity bEntity = caster.getBukkitEntity();
                if (bEntity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) bEntity;
                    EntityPickupItemEvent event = new EntityPickupItemEvent(livingEntity, item, 0);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;
                }
            }
            original.call(instance, x, y, z);
        } catch (Throwable t) {
            HybridFix.LOGGER.error("Failed to wrap setPosition", t);
        }
    }
}
