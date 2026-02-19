package io.wdsj.hybridfix.util;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import io.wdsj.hybridfix.HybridFix;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
@Mod.EventBusSubscriber(modid = HybridFix.MOD_ID)
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

    public static @NotNull WeakReference<@Nullable HybridFixDummyPlayer> getPlayerCopy(World world, BlockPos pos, EntityPlayerMP originalPlayer) {
        GameProfile profile = originalPlayer.getGameProfile();
        HybridFixDummyPlayer fakePlayer = DummyPlayerFactory.get((WorldServer) world, new GameProfile(profile.getId(), profile.getName()));
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

    public static class HybridFixDummyPlayer extends EntityPlayerMP {
        public HybridFixDummyPlayer(WorldServer world, GameProfile name) {
            super(FMLCommonHandler.instance().getMinecraftServerInstance(), world, name, new PlayerInteractionManager(world));
        }

        @Override
        public @NotNull Vec3d getPositionVector() {
            return new Vec3d(0, 0, 0);
        }

        @Override
        public boolean canUseCommand(int i, @NotNull String s) {
            return false;
        }

        @Override
        public void sendStatusMessage(@NotNull ITextComponent chatComponent, boolean actionBar) {
        }

        @Override
        public void sendMessage(@NotNull ITextComponent component) {
        }

        @Override
        public void addStat(@NotNull StatBase par1StatBase, int par2) {
        }

        @Override
        public void openGui(@NotNull Object mod, int modGuiId, @NotNull World world, int x, int y, int z) {
        }

        @Override
        public boolean isEntityInvulnerable(@NotNull DamageSource source) {
            return true;
        }

        @Override
        public boolean canAttackPlayer(@NotNull EntityPlayer player) {
            return false;
        }

        @Override
        public void onDeath(@NotNull DamageSource source) {
        }

        @Override
        public void onUpdate() {
        }

        @Override
        public Entity changeDimension(int dim, @NotNull ITeleporter teleporter) {
            return this;
        }

        @Override
        public void handleClientSettings(@NotNull CPacketClientSettings pkt) {
        }

        @Override
        public MinecraftServer getServer() {
            return FMLCommonHandler.instance().getMinecraftServerInstance();
        }

        @Override
        public void sendContainerToPlayer(@NotNull Container containerIn) {
        }

        @Override
        public boolean isPotionApplicable(@NotNull PotionEffect potion) {
            return false;
        }

        @Override
        public void loadResourcePack(@NotNull String url, @NotNull String hash) {
        }
    }

    public static class DummyPlayerFactory {
        private static final Object2ObjectOpenHashMap<GameProfile, HybridFixDummyPlayer> fakePlayers = new Object2ObjectOpenHashMap<>();

        /**
         * Get a fake player with a given username,
         * Mods should either hold weak references to the return value, or listen for a
         * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
         */
        public static HybridFixDummyPlayer get(WorldServer world, GameProfile username) {
            return fakePlayers.computeIfAbsent(username, k -> new HybridFixDummyPlayer(world, username));
        }

        private static void unloadWorld(WorldServer world) {
            fakePlayers.object2ObjectEntrySet().removeIf(entry -> entry.getValue().world == world);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof WorldServer) {
            DummyPlayerFactory.unloadWorld((WorldServer) event.getWorld());
        }
    }

    private static final String name = "[HybridFixDummy]";
    private static final GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8)), name);
}
