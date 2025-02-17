package io.wdsj.hybridfix.mixin.late.thaumcraft.taint;

import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.SpigotReflectionUtils;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockSpreadEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.blocks.world.taint.BlockTaintFibre;
import thaumcraft.common.entities.monster.tainted.EntityTaintCrawler;
import thaumcraft.common.lib.utils.BlockUtils;

@Mixin(value = EntityTaintCrawler.class)
public abstract class EntityTaintCrawlerMixin extends EntityMob implements ITaintedMob {

    @Shadow(remap = false) BlockPos lastPos;

    public EntityTaintCrawlerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "onUpdate",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void cbLogic(CallbackInfo ci) {
        if (!this.world.isRemote && this.isEntityAlive() && this.ticksExisted % 40 == 0 && this.lastPos != this.getPosition()) {
            this.lastPos = this.getPosition();
            IBlockState bs = this.world.getBlockState(this.getPosition());
            Material bm = bs.getMaterial();
            if (!bs.getBlock().isLeaves(bs, this.world, this.getPosition()) && !bm.isLiquid() && bm != ThaumcraftMaterials.MATERIAL_TAINT && (this.world.isAirBlock(this.getPosition()) || bs.getBlock().isReplaceable(this.world, this.getPosition()) || bs.getBlock() instanceof BlockFlower || bs.getBlock() instanceof IPlantable) && BlockUtils.isAdjacentToSolidBlock(this.world, this.getPosition()) && !BlockTaintFibre.isOnlyAdjacentToTaint(this.world, this.getPosition())) {
                org.bukkit.World bWorld = ((IWorldGetter) this.world).getWorld();
                final BlockPos pos = this.getPosition();
                BlockState blockState = bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getState();
                blockState.setType(SpigotReflectionUtils.CraftMagicNumbers_getMaterial(BlocksTC.taintFibre));
                BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    blockState.update(true);
                }
            }
        }
        super.onUpdate();
        ci.cancel();
    }
}
