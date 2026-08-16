package io.wdsj.hybridfix.raknetify;

import io.wdsj.hybridfix.HybridFix;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packet channel mapping for protocol 340, aka. 1.12.2
 */
public final class RaknetifyMultiChannel112 {
    private static final int UNKNOWN = Integer.MAX_VALUE;
    private static final Reference2IntOpenHashMap<Class<?>> CHANNELS = new Reference2IntOpenHashMap<>();
    private static final Set<Class<?>> WARNED = ConcurrentHashMap.newKeySet();

    static {
        CHANNELS.defaultReturnValue(UNKNOWN);

        put(-1,
                SPacketKeepAlive.class, CPacketKeepAlive.class, SPacketDisconnect.class,
                SPacketResourcePackSend.class, CPacketResourcePackStatus.class,
                SPacketStatistics.class, SPacketSelectAdvancementsTab.class,
                CPacketSeenAdvancements.class);

        put(1,
                SPacketUpdateBossInfo.class, SPacketChat.class, SPacketTitle.class,
                SPacketSetExperience.class, SPacketUpdateHealth.class, SPacketCooldown.class,
                SPacketDisplayObjective.class, SPacketScoreboardObjective.class,
                SPacketUpdateScore.class, SPacketTeams.class, SPacketAdvancementInfo.class,
                SPacketTabComplete.class, SPacketPlayerListItem.class,
                SPacketPlayerListHeaderFooter.class, SPacketPlayerPosLook.class,
                SPacketTimeUpdate.class, SPacketServerDifficulty.class,
                CPacketTabComplete.class, CPacketChatMessage.class, CPacketClickWindow.class,
                CPacketEntityAction.class, CPacketClientSettings.class, CPacketClientStatus.class,
                CPacketPlayerDigging.class, CPacketInput.class);

        put(2,
                SPacketDestroyEntities.class, SPacketSpawnObject.class, SPacketAnimation.class,
                SPacketEntityStatus.class, SPacketEntity.class,
                SPacketEntity.S15PacketEntityRelMove.class,
                SPacketEntity.S16PacketEntityLook.class,
                SPacketEntity.S17PacketEntityLookMove.class,
                SPacketEntityHeadLook.class, SPacketEntityMetadata.class,
                SPacketEntityAttach.class, SPacketEntityVelocity.class,
                SPacketEntityEquipment.class, SPacketSetPassengers.class,
                SPacketEntityTeleport.class, SPacketEntityProperties.class,
                SPacketEntityEffect.class, SPacketRemoveEntityEffect.class,
                SPacketSpawnMob.class, SPacketSpawnPlayer.class, SPacketSpawnPainting.class,
                SPacketSpawnExperienceOrb.class, SPacketSpawnGlobalEntity.class,
                SPacketExplosion.class, SPacketJoinGame.class, SPacketChangeGameState.class,
                SPacketSpawnPosition.class, SPacketCollectItem.class,
                SPacketPlayerAbilities.class, SPacketCamera.class, SPacketMoveVehicle.class,
                SPacketUseBed.class, CPacketPlayerTryUseItemOnBlock.class,
                CPacketPlayerTryUseItem.class, CPacketUseEntity.class, CPacketPlayer.class,
                CPacketPlayer.Position.class, CPacketPlayer.PositionRotation.class,
                CPacketPlayer.Rotation.class, CPacketSteerBoat.class,
                CPacketVehicleMove.class, CPacketAnimation.class);

        put(3,
                SPacketCloseWindow.class, SPacketOpenWindow.class, SPacketWindowProperty.class,
                SPacketSetSlot.class, SPacketWindowItems.class, SPacketRecipeBook.class,
                SPacketPlaceGhostRecipe.class, SPacketHeldItemChange.class,
                SPacketRespawn.class, SPacketCustomPayload.class, SPacketWorldBorder.class,
                SPacketConfirmTransaction.class, CPacketCloseWindow.class,
                CPacketConfirmTransaction.class, CPacketCreativeInventoryAction.class,
                CPacketEnchantItem.class, CPacketHeldItemChange.class,
                CPacketPlaceRecipe.class, CPacketPlayerAbilities.class,
                CPacketRecipeInfo.class, CPacketSpectate.class, CPacketUpdateSign.class,
                CPacketCustomPayload.class, CPacketConfirmTeleport.class);

        put(4,
                SPacketMaps.class, SPacketCustomSound.class, SPacketSoundEffect.class,
                SPacketParticles.class);

        put(7,
                SPacketBlockAction.class, SPacketBlockBreakAnim.class, SPacketBlockChange.class,
                SPacketChunkData.class, SPacketMultiBlockChange.class, SPacketEffect.class,
                SPacketUnloadChunk.class, SPacketUpdateTileEntity.class,
                SPacketSignEditorOpen.class);
    }

    private RaknetifyMultiChannel112() {
    }

    private static void put(int channel, Class<?>... packetClasses) {
        for (Class<?> packetClass : packetClasses) {
            CHANNELS.put(packetClass, channel);
        }
    }

    public static int getPacketChannelOverride(Packet<?> packet, boolean suppressWarning) {
        if (packet == null) {
            if (!suppressWarning) {
                HybridFix.LOGGER.warn("Raknetify: packet encoder ran without a captured packet");
            }
            return 0;
        }

        // Modern Minecraft has separate death-message and combat-state packets.
        // 1.12.2 combines them, so class-only capture is insufficient here.
        if (packet instanceof SPacketCombatEvent) {
            SPacketCombatEvent combatEvent = (SPacketCombatEvent) packet;
            return combatEvent.eventType == SPacketCombatEvent.Event.ENTITY_DIED ? 1 : 2;
        }

        int channel = CHANNELS.getInt(packet.getClass());
        if (channel == UNKNOWN) {
            if (!suppressWarning && WARNED.add(packet.getClass())) {
                HybridFix.LOGGER.warn("Raknetify: unknown protocol 340 packet type {}; using ordered channel 7", packet.getClass().getName());
            }
            return 7;
        }
        return channel;
    }
}
