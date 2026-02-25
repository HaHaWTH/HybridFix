package io.wdsj.hybridfix.util.collection.player;

import io.wdsj.hybridfix.util.collection.SingleUserAreaMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;

public class PlayerChunkTracker extends SingleUserAreaMap<EntityPlayerMP> {
    private final WorldServer worldServer;
    public PlayerChunkTracker(EntityPlayerMP player, WorldServer worldServer) {
        super(player);
        this.worldServer = worldServer;
    }

    @Override
    protected void addCallback(EntityPlayerMP player, int cx, int cz) {
        this.worldServer.getPlayerChunkMap().getOrCreateEntry(cx, cz).addPlayer(player);
    }

    @Override
    protected void removeCallback(EntityPlayerMP player, int cx, int cz) {
        PlayerChunkMapEntry entry = this.worldServer.getPlayerChunkMap().getEntry(cx, cz);
        if (entry != null) {
            entry.removePlayer(player);
        }
    }
}