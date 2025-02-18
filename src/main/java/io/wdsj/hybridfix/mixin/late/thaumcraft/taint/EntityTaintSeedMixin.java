package io.wdsj.hybridfix.mixin.late.thaumcraft.taint;

import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.duck.bridge.IWorldGetter;
import io.wdsj.hybridfix.util.SpigotReflectionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeed;
import thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.world.biomes.BiomeHandler;

@Mixin(EntityTaintSeed.class)
public abstract class EntityTaintSeedMixin extends EntityMob implements ITaintedMob {

    public EntityTaintSeedMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "spawnTentacles",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true,
            remap = false
    )
    public void spawnLogicRewrite(Entity entity, CallbackInfo ci) {
        ci.cancel();
        if (this.world.getBiome(entity.getPosition()) == BiomeHandler.ELDRITCH || this.world.getBlockState(entity.getPosition()).getMaterial() == ThaumcraftMaterials.MATERIAL_TAINT || this.world.getBlockState(entity.getPosition().down()).getMaterial() == ThaumcraftMaterials.MATERIAL_TAINT) {
            EntityTaintacleSmall taintlet = new EntityTaintacleSmall(this.world);
            taintlet.setLocationAndAngles(entity.posX + (double)this.world.rand.nextFloat() - (double)this.world.rand.nextFloat(), entity.posY, entity.posZ + (double)this.world.rand.nextFloat() - (double)this.world.rand.nextFloat(), 0.0F, 0.0F);
            this.world.spawnEntity(taintlet);
            this.playSound(SoundsTC.tentacle, this.getSoundVolume(), this.getSoundPitch());
            if (this.world.getBiome(entity.getPosition()) == BiomeHandler.ELDRITCH && this.world.isAirBlock(entity.getPosition()) && BlockUtils.isAdjacentToSolidBlock(this.world, entity.getPosition())) {
                final BlockPos pos = entity.getPosition();
                BlockState blockState = ((IWorldGetter) world).getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getState();
                blockState.setType(SpigotReflectionUtils.CraftMagicNumbers_getMaterial(BlocksTC.taintFibre));
                BlockFormEvent event = new EntityBlockFormEvent(((IEntityGetter)this).getBukkitEntity(), blockState.getBlock(), blockState);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    blockState.update(true);
                }
                // this.world.setBlockState(entity.getPosition(), BlocksTC.taintFibre.getDefaultState());
            }
        }
    }
}
