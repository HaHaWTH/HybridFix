package io.wdsj.hybridfix.mixin.fix.profile_lookup;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/*
 * Fixes NPE when profile lookup fails.
 * Reference crash report:
java.lang.NullPointerException: Cannot read the array length because the return value of "com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse.getProfiles()" is null
    at com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository.findProfilesByNames(YggdrasilGameProfileRepository.java:57)
    at net.minecraft.server.management.PlayerProfileCache.lookupProfile(PlayerProfileCache.java:100)
    at net.minecraft.server.management.PlayerProfileCache.getGameProfileForUsername(PlayerProfileCache.java:177)
    at codechicken.lib.util.ServerUtils.getGameProfile(ServerUtils.java:65)
    at codechicken.lib.util.ServerUtils.isPlayerOP(ServerUtils.java:74)
    at codechicken.chunkloader.manager.ChunkLoaderManager.allowOffline(ChunkLoaderManager.java:578)
    at codechicken.chunkloader.manager.OrganiserStorage$SavedData.load(OrganiserStorage.java:187)
    at codechicken.chunkloader.manager.ChunkLoaderManager.load(ChunkLoaderManager.java:540)
    at codechicken.chunkloader.handler.ChunkLoaderEventHandler.onChunkDataLoad(ChunkLoaderEventHandler.java:44)
    at net.minecraftforge.fml.common.eventhandler.ASMEventHandler_2548_ChunkLoaderEventHandler_onChunkDataLoad_Load.invoke(.dynamic)
    at net.minecraftforge.fml.common.eventhandler.ASMEventHandler.invoke(ASMEventHandler.java:90)
    at net.minecraftforge.fml.common.eventhandler.EventBus.post(EventBus.java:190)
    at net.minecraftforge.common.chunkio.ChunkIOProvider.syncCallback(ChunkIOProvider.java:103)
    at net.minecraftforge.common.chunkio.ChunkIOExecutor.syncChunkLoad(ChunkIOExecutor.java:98)
    at net.minecraft.world.gen.ChunkProviderServer.loadChunk(ChunkProviderServer.java:126)
    at net.minecraft.world.gen.ChunkProviderServer.loadChunk(ChunkProviderServer.java:96)
    at net.minecraft.world.gen.ChunkProviderServer.provideChunk(ChunkProviderServer.java:143)
    at net.minecraft.world.World.getChunk(World.java:444)
    at net.minecraft.world.World.getChunk(World.java:439)
    at net.minecraft.world.World.getTopSolidOrLiquidBlock(World.java:1871)
    at net.minecraft.world.WorldProvider.getRandomizedSpawnPoint(WorldProvider.java:351)
    at net.minecraft.entity.player.EntityPlayerMP.<init>(EntityPlayerMP.java:193)
    at net.minecraftforge.common.util.FakePlayer.<init>(FakePlayer.java:45)
    at net.minecraftforge.common.util.FakePlayerFactory.get(FakePlayerFactory.java:74)
    at blusunrize.immersiveengineering.common.util.FakePlayerUtil.onLoad(FakePlayerUtil.java:61)
    at net.minecraftforge.fml.common.eventhandler.ASMEventHandler_671_FakePlayerUtil_onLoad_Load.invoke(.dynamic)
    at net.minecraftforge.fml.common.eventhandler.ASMEventHandler.invoke(ASMEventHandler.java:90)
    at net.minecraftforge.fml.common.eventhandler.EventBus.post(EventBus.java:190)
    at net.minecraft.server.MinecraftServer.loadAllWorlds(MinecraftServer.java:382)
    at net.minecraft.server.dedicated.DedicatedServer.init(DedicatedServer.java:335)
    at net.minecraft.server.MinecraftServer.run(MinecraftServer.java:647)
    at java.lang.Thread.run(Thread.java:1583)
*/
@Mixin(value = YggdrasilGameProfileRepository.class, remap = false)
public abstract class YggdrasilGameProfileRepositoryMixin {
    @Unique
    private static final GameProfile[] EMPTY_PROFILES = new GameProfile[0];
    @WrapOperation(
            method = "findProfilesByNames",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/yggdrasil/response/ProfileSearchResultsResponse;getProfiles()[Lcom/mojang/authlib/GameProfile;"
            ),
            require = 0
    )
    private GameProfile[] redirectNullResponse(ProfileSearchResultsResponse instance, Operation<GameProfile[]> original) {
        @Nullable GameProfile[] profiles = original.call(instance);
        return profiles != null ? profiles : EMPTY_PROFILES;
    }
}
