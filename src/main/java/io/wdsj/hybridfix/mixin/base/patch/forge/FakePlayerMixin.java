package io.wdsj.hybridfix.mixin.base.patch.forge;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FakePlayer.class)
public abstract class FakePlayerMixin extends EntityPlayerMP {
    public FakePlayerMixin(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
        super(server, worldIn, profile, interactionManagerIn);
    }

    @Unique(silent = true)
    @Override
    public boolean isPotionApplicable(PotionEffect potion) {
        return false;
    }
}
