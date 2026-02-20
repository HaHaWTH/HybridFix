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
