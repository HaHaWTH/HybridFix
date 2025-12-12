package io.wdsj.hybridfix.mixin.late.epic_siege_mod.ai;

import funwayguy.epicsiegemod.ai.ESM_EntityAIPillarUp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ESM_EntityAIPillarUp.class)
public abstract class ESM_EntityAIPillarUpMixin {
    @Shadow(remap = false)
    private BlockPos blockPos;

    @Shadow(remap = false)
    private EntityLiving builder;

    @Shadow(remap = false)
    private static IBlockState pillarBlock;

    @Shadow(remap = false)
    public static int blockMeta;

    @Inject(
            method = "updateTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLiving;setPositionAndUpdate(DDD)V"
            ),
            cancellable = true
    )
    public void onUpdateTask(CallbackInfo ci) {
        BlockPos pos = this.blockPos;
        org.bukkit.entity.Entity bEntity = this.builder.getBukkitEntity();
        Block block = this.builder.world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        Material material = CraftMagicNumbers.getMaterial(pillarBlock.getBlock());
        byte meta = blockMeta < 0 ? (byte) 0 : (byte) blockMeta;
        // noinspection deprecation
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(bEntity, block, material, meta);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
