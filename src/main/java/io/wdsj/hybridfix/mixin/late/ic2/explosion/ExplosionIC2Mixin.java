package io.wdsj.hybridfix.mixin.late.ic2.explosion;

import ic2.api.event.ExplosionEvent;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2Potion;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.api.forge.HybridFixForgeApi;
import io.wdsj.hybridfix.util.reflection.ReflectionChain;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/*
 * IC2 is too stupid. D:
 */
@Mixin(value = ExplosionIC2.class, remap = false, priority = 500)
public abstract class ExplosionIC2Mixin extends Explosion {
    // @formatter:off
    @Shadow @Final private Entity exploder;

    @Shadow @Final private World worldObj;

    @Shadow @Final private float power;

    @Shadow @Final private int areaSize;

    @Shadow @Final private EntityLivingBase igniter;

    @Shadow @Final private int radiationRange;

    @Shadow @Final private double maxDistance;

    @Shadow private ChunkCache chunkCache;

    @Shadow @Final private double explosionX;

    @Shadow
    private static double getEntityHealth(Entity entity) {
        return 0;
    }

    @Shadow @Final private List<Object /* ExplosionsIC2.EntityDamage */> entitiesInRange;

    @Shadow @Final private double explosionY;

    @Shadow @Final private double explosionZ;

    @Shadow protected abstract void shootRay(double x, double y, double z, double phi, double theta, double power1, boolean killEntities, BlockPos.MutableBlockPos tmpPos);

    @Shadow @Final private DamageSource damageSource;

    @Shadow protected abstract boolean isNuclear();

    @Shadow @Final private long[][] destroyedBlockPositions;

    @Shadow
    private static int nextSetIndex(int start, long[] array, int step) {
        return 0;
    }

    @Shadow @Final private int areaX;

    @Shadow @Final private int areaZ;

    @Shadow
    private static int getAtIndex(int index, long[] array, int step) {
        return 0;
    }

    @Shadow @Final private float explosionDropRate;

    @Shadow @Final private ExplosionIC2.Type type;

    public ExplosionIC2Mixin(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions) {
        super(worldIn, entityIn, x, y, z, size, affectedPositions);
    }

    @Unique private static final String EXPLOSION_IC2_NAME = ExplosionIC2.class.getName();
    // EntityDamage
    @Unique private static final Constructor<?> EntityDamage_constructor = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .params(Entity.class, int.class, double.class)
            .accessible(true)
            .constructor();
    @Unique private static final Field EntityDamage_field_distance = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .name("distance")
            .accessible(true)
            .declaredField();
    @Unique private static final Field EntityDamage_field_entity = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .name("entity")
            .accessible(true)
            .declaredField();
    @Unique private static final Field EntityDamage_field_damage = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .name("damage")
            .accessible(true)
            .declaredField();
    @Unique private static final Field EntityDamage_field_motionX = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .name("motionX")
            .accessible(true)
            .declaredField();
    @Unique private static final Field EntityDamage_field_motionY = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .name("motionY")
            .accessible(true)
            .declaredField();
    @Unique private static final Field EntityDamage_field_motionZ = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$EntityDamage")
            .name("motionZ")
            .accessible(true)
            .declaredField();
    // XZPosition
    @Unique private static final Constructor<?> XZposition_constructor = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$XZposition")
            .params(int.class, int.class)
            .accessible(true)
            .constructor();
    @Unique private static final Field XZposition_field_x = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$XZposition")
            .name("x")
            .accessible(true)
            .declaredField();
    @Unique private static final Field XZposition_field_z = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$XZposition")
            .name("z")
            .accessible(true)
            .declaredField();
    // DropData
    @Unique private static final Constructor<?> DropData_constructor = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$DropData")
            .params(int.class, int.class)
            .accessible(true)
            .constructor();
    @Unique private static final Method DropData_method_add = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$DropData")
            .name("add")
            .params(int.class, int.class)
            .accessible(true)
            .declaredMethod();
    @Unique private static final Field DropData_field_n = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$DropData")
            .name("n")
            .accessible(true)
            .declaredField();
    @Unique private static final Field DropData_field_maxY = ReflectionChain.fromClass(EXPLOSION_IC2_NAME + "$DropData")
            .name("maxY")
            .accessible(true)
            .declaredField();
    // @formatter:on

    /**
     * Target IC2 version: 2.8.222-ex112
     *
     * @author Creeam
     * @reason Fuck up IC2 explosion logic
     */
    @Overwrite
    public void doExplosion() {
        if (this.power <= 0.0F) return;
        try {
            ExplosionEvent event = new ExplosionEvent(this.worldObj, this.exploder, this.getPosition(), this.power, this.igniter, this.radiationRange, this.maxDistance);
            if (MinecraftForge.EVENT_BUS.post(event)) return;
            int range = this.areaSize / 2;
            BlockPos pos = new BlockPos(this.getPosition());
            BlockPos start = pos.add(-range, -range, -range);
            BlockPos end = pos.add(range, range, range);
            this.chunkCache = new ChunkCache(this.worldObj, start, end, 0);
            // Fire ExplosionStart event
            if (ForgeEventFactory.onExplosionStart(this.worldObj, this)) return;
            for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(start, end))) {
                if (entity instanceof EntityLivingBase || entity instanceof EntityItem) {
                    int distance = (int) (Util.square(entity.posX - this.explosionX) + Util.square(entity.posY - this.explosionY) + Util.square(entity.posZ - this.explosionZ));
                    double health = getEntityHealth(entity);
                    this.entitiesInRange.add(EntityDamage_constructor.newInstance(entity, distance, health));
                }
            }

            boolean entitiesAreInRange = !entitiesInRange.isEmpty();
            if (entitiesAreInRange) {
                entitiesInRange.sort((o1, o2) -> {
                    try {
                        return EntityDamage_field_distance.getInt(o1) - EntityDamage_field_distance.getInt(o2);
                    } catch (Exception e) {
                        HybridFix.LOGGER.warn("Error occurred while getting value of field distance", e);
                        return 0;
                    }
                });
            }

            int steps = (int) Math.ceil(Math.PI / Math.atan((double) 1.0F / this.maxDistance));
            BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();

            for (int phi_n = 0; phi_n < 2 * steps; ++phi_n) {
                for (int theta_n = 0; theta_n < steps; ++theta_n) {
                    double phi = (Math.PI * 2D) / (double) steps * (double) phi_n;
                    double theta = Math.PI / (double) steps * (double) theta_n;
                    this.shootRay(this.explosionX, this.explosionY, this.explosionZ, phi, theta, this.power, entitiesAreInRange && phi_n % 8 == 0 && theta_n % 8 == 0, tmpPos);
                }
            }


            IC2.network.get(true).initiateExplosionEffect(this.worldObj, this.getPosition(), this.type);
            Random rng = this.worldObj.rand;
            boolean doDrops = this.worldObj.getGameRules().getBoolean("doTileDrops");
            Map<Object /* ExplosionIC2.XZposition */, Map<ItemComparableItemStack, Object /* ExplosionIC2.DropData */>> blocksToDrop = new HashMap<>();

            Map<BlockPos, Boolean> affectedBlockMap = new HashMap<>();
            for (int y = 0; y < this.destroyedBlockPositions.length; ++y) {
                long[] bitSet = this.destroyedBlockPositions[y];
                if (bitSet == null) continue;
                for (int index = -2; (index = nextSetIndex(index + 2, bitSet, 2)) != -1; ) {
                    int realIndex = index / 2;
                    int z = realIndex / this.areaSize;
                    int x = realIndex - z * this.areaSize;
                    x += this.areaX;
                    z += this.areaZ;
                    tmpPos.setPos(x, y, z);
                    IBlockState state = this.chunkCache.getBlockState(tmpPos);
                    Block block = state.getBlock();
                    final BlockPos _pos = new BlockPos(x, y, z);
                    if (doDrops && block.canDropFromExplosion(this) && getAtIndex(index, bitSet, 2) == 1) {
                        affectedBlockMap.put(_pos, true);
                    } else {
                        affectedBlockMap.put(_pos, false);
                    }
                    // noinspection StatementWithEmptyBody
                    if (this.power < 20.0F) { // Leave it here as IC2 does it
                    }
                }
            }

            List<BlockPos> vanillaAffectedBlocks = getAffectedBlockPositions();
            clearAffectedBlockPositions();
            vanillaAffectedBlocks.addAll(affectedBlockMap.keySet());

            List<Entity> affectedEntities = entitiesInRange.stream()
                    .map(entityDamage -> {
                        try {
                            return (Entity) EntityDamage_field_entity.get(entityDamage);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            net.minecraftforge.event.world.ExplosionEvent.Detonate detonateEvent = new net.minecraftforge.event.world.ExplosionEvent.Detonate(world, this, affectedEntities);
            HybridFixForgeApi.getApi().setVanillaExplosionEventDetonate(detonateEvent, false);
            MinecraftForge.EVENT_BUS.post(detonateEvent);

            Set<BlockPos> finalAffectedBlocks = new ObjectOpenHashSet<>(getAffectedBlockPositions());
            affectedBlockMap.entrySet().removeIf(entry ->
                    !finalAffectedBlocks.contains(entry.getKey())
            );
            this.entitiesInRange.removeIf(entry -> {
                try {
                    return !affectedEntities.contains((Entity) EntityDamage_field_entity.get(entry));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            for (Object /* ExplosionIC2.EntityDamage */ entry : this.entitiesInRange) {
                Entity entity = (Entity) EntityDamage_field_entity.get(entry);
                entity.attackEntityFrom(this.damageSource, (float) EntityDamage_field_damage.getDouble(entry));
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (this.isNuclear() && this.igniter != null && player == this.igniter && player.getHealth() <= 0.0F) {
                        IC2.achievements.issueAchievement(player, "dieFromOwnNuke");
                    }
                }

                // noinspection SuspiciousNameCombination
                double motionSq = Util.square(entity.motionX) + Util.square(entity.motionY) + Util.square(entity.motionZ);
                double reduction = motionSq > (double) 3600.0F ? Math.sqrt((double) 3600.0F / motionSq) : (double) 1.0F;
                entity.motionX += EntityDamage_field_motionX.getDouble(entry) * reduction;
                entity.motionY += EntityDamage_field_motionY.getDouble(entry) * reduction;
                entity.motionZ += EntityDamage_field_motionZ.getDouble(entry) * reduction;
            }

            if (this.isNuclear() && this.radiationRange >= 1) {
                for (EntityLiving entity : this.worldObj.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.explosionX - (double) this.radiationRange, this.explosionY - (double) this.radiationRange, this.explosionZ - (double) this.radiationRange, this.explosionX + (double) this.radiationRange, this.explosionY + (double) this.radiationRange, this.explosionZ + (double) this.radiationRange))) {
                    if (!ItemArmorHazmat.hasCompleteHazmat(entity)) {
                        double distance = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ);
                        int hungerLength = (int) ((double) 120.0F * ((double) this.radiationRange - distance));
                        int poisonLength = (int) ((double) 80.0F * ((double) (this.radiationRange / 3) - distance));
                        if (hungerLength >= 0) {
                            entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, hungerLength, 0));
                        }

                        if (poisonLength >= 0) {
                            IC2Potion.radiation.applyTo(entity, poisonLength, 0);
                        }
                    }
                }
            }

            for (Map.Entry<BlockPos, Boolean> entry : affectedBlockMap.entrySet()) {
                BlockPos blockPos = entry.getKey();
                IBlockState state = this.chunkCache.getBlockState(blockPos);
                Block block = state.getBlock();
                if (entry.getValue()) {
                    for (ItemStack stack : StackUtil.getDrops(this.worldObj, tmpPos, state, block, 0)) {
                        if (!(rng.nextFloat() > this.explosionDropRate)) {
                            Object /* ExplosionIC2.XZposition */ xZposition = XZposition_constructor.newInstance((int) x / 2, (int) z / 2);
                            Map<ItemComparableItemStack, Object /* ExplosionIC2.DropData */> map = blocksToDrop.computeIfAbsent(xZposition, k -> new HashMap<>());

                            ItemComparableItemStack isw = new ItemComparableItemStack(stack, false);
                            /* ExplosionIC2.DropData */
                            Object data = map.get(isw);
                            if (data == null) {
                                data = DropData_constructor.newInstance(StackUtil.getSize(stack), (int) y);
                                map.put(isw.copy(), data);
                            } else {
                                DropData_method_add.invoke(data, StackUtil.getSize(stack), (int) y);
                            }
                        }
                    }
                }
                block.onBlockExploded(this.worldObj, blockPos, this);
            }

            for (Map.Entry<Object /* ExplosionIC2.XZposition */, Map<ItemComparableItemStack, Object /* ExplosionIC2.DropData */>> entry : blocksToDrop.entrySet()) {
                Object xZposition = entry.getKey();

                for (Map.Entry<ItemComparableItemStack, Object /* ExplosionIC2.DropData */> entry2 : entry.getValue().entrySet()) {
                    ItemComparableItemStack isw = entry2.getKey();

                    int stackSize;
                    for (int count = DropData_field_n.getInt(entry2.getValue()); count > 0; count -= stackSize) {
                        stackSize = Math.min(count, 64);
                        EntityItem entityitem = new EntityItem(this.worldObj, ((float) XZposition_field_x.getInt(xZposition) + this.worldObj.rand.nextFloat()) * 2.0F, (double) DropData_field_maxY.getInt(entry2.getValue()) + (double) 0.5F, ((float) XZposition_field_z.getInt(xZposition) + this.worldObj.rand.nextFloat()) * 2.0F, isw.toStack(stackSize));
                        entityitem.setDefaultPickupDelay();
                        this.worldObj.spawnEntity(entityitem);
                    }
                }
            }
        } catch (Throwable th) {
            HybridFix.LOGGER.error("Error in ExplosionIC2: ", th);
        }
    }
}
