package io.wdsj.hybridfix.mixin.perf.server.universal;

import io.wdsj.hybridfix.util.collection.SingleUserAreaMap;
import io.wdsj.hybridfix.util.collection.player.PlayerChunkTracker;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(value = PlayerChunkMap.class, priority = 999)
public abstract class PlayerChunkMapMixin {

    @Shadow private int playerViewRadius;
    @Shadow @Final private List<EntityPlayerMP> players;
    @Shadow public abstract PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ);
    @Shadow @Nullable public abstract PlayerChunkMapEntry getEntry(int x, int z);
    @Shadow protected abstract void markSortPending();
    @Shadow @Final private WorldServer world;
    @Unique
    private final Map<EntityPlayerMP, SingleUserAreaMap<EntityPlayerMP>> hybridfix$trackers = new Reference2ReferenceOpenHashMap<>();

    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private void onAddPlayer(EntityPlayerMP player, CallbackInfo ci) {
        int cx = (int) player.posX >> 4;
        int cz = (int) player.posZ >> 4;
        player.managedPosX = player.posX;
        player.managedPosZ = player.posZ;

        SingleUserAreaMap<EntityPlayerMP> tracker = new PlayerChunkTracker(player, this.world);
        this.hybridfix$trackers.put(player, tracker);

        tracker.add(cx, cz, this.playerViewRadius);

        this.players.add(player);
        this.markSortPending();
        ci.cancel();
    }

    @Inject(method = "removePlayer", at = @At("HEAD"), cancellable = true)
    private void onRemovePlayer(EntityPlayerMP player, CallbackInfo ci) {
        SingleUserAreaMap<EntityPlayerMP> tracker = this.hybridfix$trackers.remove(player);
        if (tracker != null) {
            tracker.remove();
        }

        this.players.remove(player);
        this.markSortPending();
        ci.cancel();
    }

    @Inject(method = "updateMovingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateMovingPlayer(EntityPlayerMP player, CallbackInfo ci) {
        double d0 = player.managedPosX - player.posX;
        double d1 = player.managedPosZ - player.posZ;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            int cx = (int) player.posX >> 4;
            int cz = (int) player.posZ >> 4;
            int oldCx = (int) player.managedPosX >> 4;
            int oldCz = (int) player.managedPosZ >> 4;

            if (cx != oldCx || cz != oldCz) {
                SingleUserAreaMap<EntityPlayerMP> tracker = this.hybridfix$trackers.get(player);
                if (tracker != null) {
                    tracker.update(cx, cz, this.playerViewRadius);
                }
                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
                this.markSortPending();
            }
        }
        ci.cancel();
    }

    @Inject(method = "setPlayerViewRadius", at = @At("HEAD"), cancellable = true)
    private void onSetPlayerViewRadius(int radius, CallbackInfo ci) {
        radius = MathHelper.clamp(radius, 3, 32);

        if (radius != this.playerViewRadius) {
            for (EntityPlayerMP player : new ObjectArrayList<>(this.players)) {
                SingleUserAreaMap<EntityPlayerMP> tracker = this.hybridfix$trackers.get(player);
                if (tracker != null) {
                    tracker.update(tracker.getLastChunkX(), tracker.getLastChunkZ(), radius);
                }
            }
            this.playerViewRadius = radius;
            this.markSortPending();
        }
        ci.cancel();
    }
}