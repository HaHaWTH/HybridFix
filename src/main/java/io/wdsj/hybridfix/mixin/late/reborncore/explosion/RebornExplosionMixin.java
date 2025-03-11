package io.wdsj.hybridfix.mixin.late.reborncore.explosion;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import reborncore.RebornCore;
import reborncore.common.explosion.RebornExplosion;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrite RebornCore's explosion processing with more checks to make it safer.
 */
@Mixin(RebornExplosion.class)
public abstract class RebornExplosionMixin extends Explosion {
    public RebornExplosionMixin(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions) {
        super(worldIn, entityIn, x, y, z, size, affectedPositions);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void init(BlockPos center, World world, int radius, CallbackInfo ci) {
        for (int tx = -radius; tx < radius + 1; tx++) {
            for (int ty = -radius; ty < radius + 1; ty++) {
                for (int tz = -radius; tz < radius + 1; tz++) {
                    if (Math.sqrt(Math.pow(tx, 2) + Math.pow(ty, 2) + Math.pow(tz, 2)) <= radius - 2) {
                        BlockPos pos = center.add(tx, ty, tz);
                        IBlockState state = world.getBlockState(pos);
                        Block block = state.getBlock();
                        float hardness = state.getBlockHardness(world, pos);
                        if (block != Blocks.BEDROCK && block != Blocks.AIR && hardness >= 0.0F && hardness < 50.0F) {
                            affectedBlockPositions.add(pos);
                        }
                    }
                }
            }
        }
    }

    @Inject(
            method = "explode",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    public void rewriteExplodeLogic(CallbackInfo ci) {
        ci.cancel();
        ExplosionEvent.Start start = new ExplosionEvent.Start(this.world, this);
        if (MinecraftForge.EVENT_BUS.post(start)) {
            return;
        }
        StopWatch watch = new StopWatch();
        watch.start();
        ExplosionEvent.Detonate event = new ExplosionEvent.Detonate(this.world, this, new ArrayList<>());
        MinecraftForge.EVENT_BUS.post(event);
        for (BlockPos pos : affectedBlockPositions) {
            IBlockState state = this.world.getBlockState(pos);
            Block block = state.getBlock();
            if (block != Blocks.BEDROCK && block != Blocks.AIR) {
                block.onBlockExploded(this.world, pos, this);
                this.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }
        }
        RebornCore.logHelper.info("The explosion took " + watch + " to explode");
    }

    @Inject(
            method = "getAffectedBlockPositions",
            at = @At("HEAD"),
            cancellable = true
    )
    public void rewriteGetAffectedBlockPositions(CallbackInfoReturnable<List<BlockPos>> cir) {
        cir.setReturnValue(affectedBlockPositions);
    }
}
