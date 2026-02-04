package io.wdsj.hybridfix.mixin.late.thaumcraft.taint;

import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockSpreadEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blocks.IBlockFacing;
import thaumcraft.common.blocks.world.taint.BlockTaintFibre;
import thaumcraft.common.blocks.world.taint.BlockTaintLog;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeed;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.world.aura.AuraHandler;

import static thaumcraft.common.blocks.world.taint.TaintHelper.isAtTaintSeedEdge;
import static thaumcraft.common.blocks.world.taint.TaintHelper.isNearTaintSeed;

@Mixin(value = TaintHelper.class, priority = 999)
public abstract class TaintHelperMixin {

    @Inject(
            method = "spreadFibres(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void spreadFibres(World world, BlockPos pos, boolean ignore, CallbackInfo ci) {
        ci.cancel();
        if (ignore || !ModConfig.CONFIG_MISC.wussMode) {
            float mod = 0.001F + AuraHandler.getFluxSaturation(world, pos) * 2.0F;
            if (ignore || !(world.rand.nextFloat() > ModConfig.CONFIG_WORLD.taintSpreadRate / 100.0F * mod)) {
                if (isNearTaintSeed(world, pos)) {
                    int xx = pos.getX() + world.rand.nextInt(3) - 1;
                    int yy = pos.getY() + world.rand.nextInt(3) - 1;
                    int zz = pos.getZ() + world.rand.nextInt(3) - 1;
                    BlockPos t = new BlockPos(xx, yy, zz);
                    if (t.equals(pos)) {
                        return;
                    }

                    IBlockState bs = io.wdsj.hybridfix.util.Utils.getBlockStateIfLoaded(world, t);
                    if (bs == null) return;
                    Material bm = bs.getBlock().getMaterial(bs);
                    float bh = bs.getBlock().getBlockHardness(bs, world, t);
                    if (bh < 0.0F || bh > 10.0F) {
                        return;
                    }

                    org.bukkit.World bWorld = world.getWorld();
                    if (!bs.getBlock().isLeaves(bs, world, t) && !bm.isLiquid() && (world.isAirBlock(t) || bs.getBlock().isReplaceable(world, t) || bs.getBlock() instanceof BlockFlower || bs.getBlock() instanceof IPlantable) && BlockUtils.isAdjacentToSolidBlock(world, t) && !BlockTaintFibre.isOnlyAdjacentToTaint(world, t)) {
                        BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                        blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintFibre));
                        BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            blockState.update(true);
                            world.addBlockEvent(t, BlocksTC.taintFibre, 1, 0);
                            AuraHelper.drainFlux(world, t, 0.01F, false);
                        }
                        // world.setBlockState(t, BlocksTC.taintFibre.getDefaultState());
                        return;
                    }

                    if (bs.getBlock().isLeaves(bs, world, t)) {
                        EnumFacing face;
                        if ((double) world.rand.nextFloat() < 0.6 && (face = BlockUtils.getFaceBlockTouching(world, t, BlocksTC.taintLog)) != null) {
                            BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                            blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintFeature));
                            blockState.setRawData((byte) BlocksTC.taintFeature.getMetaFromState(BlocksTC.taintFeature.getDefaultState().withProperty(IBlockFacing.FACING, face.getOpposite())));
                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                blockState.update(true);
                            }
                            // world.setBlockState(t, BlocksTC.taintFeature.getDefaultState().withProperty(IBlockFacing.FACING, face.getOpposite()));
                        } else {
                            // world.setBlockState(t, BlocksTC.taintFibre.getDefaultState());
                            BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                            blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintFibre));
                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                blockState.update(true);
                                world.addBlockEvent(t, BlocksTC.taintFibre, 1, 0);
                                AuraHelper.drainFlux(world, t, 0.01F, false);
                            }
                        }

                        return;
                    }

                    if (BlockTaintFibre.isHemmedByTaint(world, t) && bs.getBlockHardness(world, t) < 5.0F) {
                        if (Utils.isWoodLog(world, t) && bs.getMaterial() != ThaumcraftMaterials.MATERIAL_TAINT) {
                            BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                            blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintLog));
                            // noinspection unchecked, deprecation
                            blockState.setRawData((byte) BlocksTC.taintLog.getMetaFromState(BlocksTC.taintLog.getDefaultState().withProperty(BlockTaintLog.AXIS, BlockUtils.getBlockAxis(world, t))));
                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                blockState.update(true);
                            }
                            // world.setBlockState(t, BlocksTC.taintLog.getDefaultState().withProperty(BlockTaintLog.AXIS, BlockUtils.getBlockAxis(world, t)));
                            return;
                        }

                        if (bs.getBlock() == Blocks.RED_MUSHROOM_BLOCK || bs.getBlock() == Blocks.BROWN_MUSHROOM_BLOCK || bm == Material.GOURD || bm == Material.CACTUS || bm == Material.CORAL || bm == Material.SPONGE || bm == Material.WOOD) {
                            BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                            blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintCrust));
                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                blockState.update(true);
                                world.addBlockEvent(t, BlocksTC.taintCrust, 1, 0);
                                AuraHelper.drainFlux(world, t, 0.01F, false);
                            }
                            // world.setBlockState(t, BlocksTC.taintCrust.getDefaultState());
                            return;
                        }

                        if (bm == Material.SAND || bm == Material.GROUND || bm == Material.GRASS || bm == Material.CLAY) {
                            BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                            blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintSoil));
                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                blockState.update(true);
                                world.addBlockEvent(t, BlocksTC.taintSoil, 1, 0);
                                AuraHelper.drainFlux(world, t, 0.01F, false);
                            }
                            // world.setBlockState(t, BlocksTC.taintSoil.getDefaultState());
                            return;
                        }

                        if (bm == Material.ROCK) {
                            BlockState blockState = bWorld.getBlockAt(t.getX(), t.getY(), t.getZ()).getState();
                            blockState.setType(CraftMagicNumbers.getMaterial(BlocksTC.taintRock));
                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), blockState);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                blockState.update(true);
                                world.addBlockEvent(t, BlocksTC.taintRock, 1, 0);
                                AuraHelper.drainFlux(world, t, 0.01F, false);
                            }
                            // world.setBlockState(t, BlocksTC.taintRock.getDefaultState());
                            return;
                        }
                    }

                    if ((bs.getBlock() == BlocksTC.taintSoil || bs.getBlock() == BlocksTC.taintRock) && world.isAirBlock(t.up()) && AuraHelper.getFlux(world, t) >= 5.0F && (double) world.rand.nextFloat() < (double) (ModConfig.CONFIG_WORLD.taintSpreadRate / 100.0F) * 0.33 && isAtTaintSeedEdge(world, t)) {
                        EntityTaintSeed e = new EntityTaintSeed(world);
                        e.setLocationAndAngles((float) t.getX() + 0.5F, t.up().getY(), (float) t.getZ() + 0.5F, (float) world.rand.nextInt(360), 0.0F);
                        if (e.getCanSpawnHere()) {
                            AuraHelper.drainFlux(world, t, 5.0F, false);
                            world.spawnEntity(e);
                        }
                    }
                }

            }
        }
    }

}
