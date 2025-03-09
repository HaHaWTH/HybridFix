package io.wdsj.hybridfix.handler.fix.respawn.thaumcraft;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TCCapabilityHandler {
    private final HashMap<UUID, List<NBTTagCompound>> nbtMap = new HashMap<>();
    @SubscribeEvent(priority = EventPriority.LOW)
    public void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getObject();
            if (!player.isDead) return; // Whether we are switching world, this is called in removeEntityDangerously()
            NBTTagCompound knowledge = ThaumcraftCapabilities.getKnowledge(player).serializeNBT();
            NBTTagCompound nbtWarp = ThaumcraftCapabilities.getWarp(player).serializeNBT();
            List<NBTTagCompound> nbtToRestore = new ObjectArrayList<>();
            nbtToRestore.add(knowledge);
            nbtToRestore.add(nbtWarp);
            nbtMap.put(player.getUniqueID(), nbtToRestore);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        UUID uuid = player.getUniqueID();
        if (nbtMap.containsKey(uuid)) {
            List<NBTTagCompound> nbts = this.nbtMap.get(uuid);
            NBTTagCompound knowledge = nbts.get(0);
            NBTTagCompound nbtWarp = nbts.get(1);
            ThaumcraftCapabilities.getKnowledge(player).deserializeNBT(knowledge);
            ThaumcraftCapabilities.getWarp(player).deserializeNBT(nbtWarp);
            nbtMap.remove(uuid);
        }
    }

    @SubscribeEvent
    public void onQuit(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.player.getUniqueID();
        nbtMap.remove(uuid);
    }
}
