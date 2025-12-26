package io.wdsj.hybridfix.util;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * This class should <b>ONLY</b> be used for non {@link org.bukkit.event.player.PlayerEvent} events.
 * As some server software (like CatServer) will ignore PlayerEvents involved by FakePlayer.
 *
 * @apiNote To bypass server FakePlayer checks, use {@link io.wdsj.hybridfix.util.reflection.HybridReflectionUtils#callEventDirect(Event)}
 */
public class HybridFixFakePlayer {
    private HybridFixFakePlayer() {
    }

    private static final Cache<String, GameProfile> profileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public static WeakReference<FakePlayer> get(World world) {
        return new WeakReference<>(FakePlayerFactory.get((WorldServer) world, profile));
    }

    public static WeakReference<FakePlayer> get(World world, BlockPos pos) {
        FakePlayer player = FakePlayerFactory.get((WorldServer) world, profile);
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return new WeakReference<>(player);
    }

    public static @NotNull WeakReference<@Nullable FakePlayer> getPlayerCopy(World world, BlockPos pos, EntityPlayerMP originalPlayer) {
        GameProfile profile = originalPlayer.getGameProfile();
        FakePlayer fakePlayer = FakePlayerFactory.get((WorldServer) world, profile);
        fakePlayer.posX = pos.getX();
        fakePlayer.posY = pos.getY();
        fakePlayer.posZ = pos.getZ();
        return new WeakReference<>(fakePlayer);
    }

    public static @NotNull WeakReference<@Nullable FakePlayer> get(World world, BlockPos pos, @NotNull String name) {
        try {
            FakePlayer player = FakePlayerFactory.get((WorldServer) world, profileCache.get(name, () -> new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8)), name)));
            player.posX = pos.getX();
            player.posY = pos.getY();
            player.posZ = pos.getZ();
            return new WeakReference<>(player);
        } catch (ExecutionException e) {
            SneakyThrow.sneaky(e);
            return null; // Never reached
        }
    }

    private static final String name = "[HybridFixDummy]";
    private static final GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8)), name);
}
