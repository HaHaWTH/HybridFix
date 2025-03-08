package io.wdsj.hybridfix.util;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * This class should <b>ONLY</b> be used for non {@link org.bukkit.event.player.PlayerEvent} events.
 * As some server software (like CatServer) will ignore PlayerEvents involved by FakePlayer.
 */
public class HybridFixFakePlayer {
    private HybridFixFakePlayer() {
    }

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

    private static final String name = "HybridFixDummy";
    private static final GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8)), name);
}
