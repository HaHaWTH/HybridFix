package io.wdsj.hybridfix.handler.fix.respawn.baubles;

import baubles.api.BaublesApi;
import baubles.api.cap.BaublesContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

@Deprecated
public class BaublesCapabilityHandler {
    private final HashMap<UUID, NBTTagCompound> nbtMap = new HashMap<>();
    @SubscribeEvent(priority = EventPriority.LOW)
    public void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getObject();
            if (!player.isDead) return; // Whether we are switching world, this is called in removeEntityDangerously()
            BaublesContainer bco = (BaublesContainer) BaublesApi.getBaublesHandler(player);
            nbtMap.put(player.getUniqueID(), bco.serializeNBT());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        UUID uuid = player.getUniqueID();
        if (nbtMap.containsKey(uuid)) {
            BaublesContainer bco = (BaublesContainer) BaublesApi.getBaublesHandler(player);
            bco.deserializeNBT(nbtMap.get(uuid));
            nbtMap.remove(uuid);
        }
    }

    @SubscribeEvent
    public void onQuit(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.player.getUniqueID();
        nbtMap.remove(uuid);
    }
}
