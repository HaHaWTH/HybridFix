package io.wdsj.hybridfix.mixin.fix.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import static net.minecraft.entity.Entity.*;

public class CBRespawnFixLogic {
    private CBRespawnFixLogic() {
    }
    public static void fixRespawn(EntityPlayerMP playerIn) {
        try {
            playerIn.getDataManager().lock.writeLock().lock();
            playerIn.getDataManager().entries.clear();
        } finally {
            playerIn.getDataManager().lock.writeLock().unlock();
        }
        playerIn.getDataManager().empty = true;
        playerIn.getDataManager().setClean();
        // Entity data params
        playerIn.getDataManager().register(FLAGS, Byte.valueOf((byte)0));
        playerIn.getDataManager().register(AIR, Integer.valueOf(300));
        playerIn.getDataManager().register(CUSTOM_NAME_VISIBLE, Boolean.valueOf(false));
        playerIn.getDataManager().register(CUSTOM_NAME, "");
        playerIn.getDataManager().register(SILENT, Boolean.valueOf(false));
        playerIn.getDataManager().register(NO_GRAVITY, Boolean.valueOf(false));

        playerIn.entityInit();

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityEvent.EntityConstructing(playerIn));
        ((EntityCapabilityAccessor) (Entity) playerIn).setCapabilities(net.minecraftforge.event.ForgeEventFactory.gatherCapabilities(playerIn));
    }
}
