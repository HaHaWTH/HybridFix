package io.wdsj.hybridfix.logic.respawn;

import io.wdsj.hybridfix.mixin.fix.respawn.EntityCapabilityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;

public class CBRespawnFixLogic {
    private CBRespawnFixLogic() {
    }
    public static void simulateVanillaRespawn(EntityPlayerMP playerIn) {
        final EntityDataManager dataManager = playerIn.getDataManager();
        // Capture vanilla values
        // Entity
        byte capturedFlags = dataManager.get(Entity.FLAGS);
        int capturedAirTicks = dataManager.get(Entity.AIR);
        String capturedCustomName = dataManager.get(Entity.CUSTOM_NAME);
        boolean capturedCustomNameVisible = dataManager.get(Entity.CUSTOM_NAME_VISIBLE);
        boolean capturedSilent = dataManager.get(Entity.SILENT);
        boolean capturedNoGravity = dataManager.get(Entity.NO_GRAVITY);
        // EntityLivingBase
        byte capturedHandStates = dataManager.get(EntityLivingBase.HAND_STATES);
        float capturedHealth = dataManager.get(EntityLivingBase.HEALTH);
        int capturedPotionEffects = dataManager.get(EntityLivingBase.POTION_EFFECTS);
        boolean capturedHideParticles = dataManager.get(EntityLivingBase.HIDE_PARTICLES);
        int capturedArrowCount = dataManager.get(EntityLivingBase.ARROW_COUNT_IN_ENTITY);
        // EntityPlayer
        float capturedAbsorptionAmount = dataManager.get(EntityPlayer.ABSORPTION);
        int capturedScore = dataManager.get(EntityPlayer.PLAYER_SCORE);
        byte capturedPlayerModelFlag = dataManager.get(EntityPlayer.PLAYER_MODEL_FLAG);
        byte capturedMainHand = dataManager.get(EntityPlayer.MAIN_HAND);
        NBTTagCompound capturedLeftShoulder = dataManager.get(EntityPlayer.LEFT_SHOULDER_ENTITY);
        NBTTagCompound capturedRightShoulder = dataManager.get(EntityPlayer.RIGHT_SHOULDER_ENTITY);

        try {
            dataManager.lock.writeLock().lock();
            dataManager.entries.clear();
        } finally {
            dataManager.lock.writeLock().unlock();
        }
        dataManager.empty = true;
        dataManager.setClean();

        // Restore captured vanilla values
        // Entity
        dataManager.register(Entity.FLAGS, capturedFlags);
        dataManager.register(Entity.AIR, capturedAirTicks);
        dataManager.register(Entity.CUSTOM_NAME, capturedCustomName);
        dataManager.register(Entity.CUSTOM_NAME_VISIBLE, capturedCustomNameVisible);
        dataManager.register(Entity.SILENT, capturedSilent);
        dataManager.register(Entity.NO_GRAVITY, capturedNoGravity);
        // EntityLivingBase
        dataManager.register(EntityLivingBase.HAND_STATES, capturedHandStates);
        dataManager.register(EntityLivingBase.HEALTH, capturedHealth);
        dataManager.register(EntityLivingBase.POTION_EFFECTS, capturedPotionEffects);
        dataManager.register(EntityLivingBase.HIDE_PARTICLES, capturedHideParticles);
        dataManager.register(EntityLivingBase.ARROW_COUNT_IN_ENTITY, capturedArrowCount);
        // EntityPlayer
        dataManager.register(EntityPlayer.ABSORPTION, capturedAbsorptionAmount);
        dataManager.register(EntityPlayer.PLAYER_SCORE, capturedScore);
        dataManager.register(EntityPlayer.PLAYER_MODEL_FLAG, capturedPlayerModelFlag);
        dataManager.register(EntityPlayer.MAIN_HAND, capturedMainHand);
        dataManager.register(EntityPlayer.LEFT_SHOULDER_ENTITY, capturedLeftShoulder);
        dataManager.register(EntityPlayer.RIGHT_SHOULDER_ENTITY, capturedRightShoulder);

        MinecraftForge.EVENT_BUS.post(new EntityEvent.EntityConstructing(playerIn));
    }
    public static void regatherCapabilities(EntityPlayerMP playerIn) {
        ((EntityCapabilityAccessor) (Entity) playerIn).setCapabilities(ForgeEventFactory.gatherCapabilities(playerIn));
    }
}
