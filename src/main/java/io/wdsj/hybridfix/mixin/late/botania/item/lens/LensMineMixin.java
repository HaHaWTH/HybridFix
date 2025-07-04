package io.wdsj.hybridfix.mixin.late.botania.item.lens;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.common.item.lens.LensMine;

/**
 * @see <a href="https://bug.mcmod.cn/item/20181312.html">bugs.mcmod.cn</a>
 */
@Mixin(LensMine.class)
public abstract class LensMineMixin {

    @Inject(
            method = "collideBurst",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/NonNullList;create()Lnet/minecraft/util/NonNullList;",
                    remap = true
            ),
            cancellable = true,
            remap = false,
            allow = 1
    )
    public void checkBurst(IManaBurst burst, EntityThrowable entity, RayTraceResult rtr, boolean isManaBlock, boolean dead, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        World world = entity.world;
        BlockPos collidePos = rtr.getBlockPos();

        org.bukkit.block.Block bukkitBlock = ((IWorldGetter) (world)).getWorld().getBlockAt(collidePos.getX(), collidePos.getY(), collidePos.getZ());
        EntityLivingBase thrower = entity.getThrower();
        if (thrower instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) thrower;
            Player bukkitPlayer = (CraftPlayer) ((IEntityGetter) serverPlayer).getBukkitEntity();
            BlockBreakEvent event = new BlockBreakEvent(bukkitBlock, bukkitPlayer);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        } else {
            Entity nonNullThrower = thrower != null ? thrower : entity;
            org.bukkit.entity.Entity bEntity = ((IEntityGetter) nonNullThrower).getBukkitEntity();
            // noinspection deprecation
            EntityChangeBlockEvent event = new EntityChangeBlockEvent(bEntity, bukkitBlock, Material.AIR, (byte) 0);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }
}
