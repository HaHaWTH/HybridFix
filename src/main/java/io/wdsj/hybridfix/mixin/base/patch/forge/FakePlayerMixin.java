package io.wdsj.hybridfix.mixin.base.patch.forge;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FakePlayer.class)
public abstract class FakePlayerMixin extends EntityPlayerMP {
    public FakePlayerMixin(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
        super(server, worldIn, profile, interactionManagerIn);
    }

    /**
     * Adding potion effects will cause packet send through {@link EntityPlayerMP#connection}, this won't work for fame players
     * as all connections are null for fake players.
     * @see org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer#addPotionEffect(org.bukkit.potion.PotionEffect)
     */
    @Unique(silent = true)
    @Override
    public boolean isPotionApplicable(@NotNull PotionEffect potion) {
        return false;
    }

    /**
     * CB's setResourcePack doesn't check the nullability of the connection, this will cause NPE.
     * @see org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer#setResourcePack(String, byte[])
     */
    @Unique(silent = true)
    @Override
    public void loadResourcePack(@NotNull String url, @NotNull String hash) {}

    /**
     * CB's updateInventory doesn't check the nullability of the connection, this will cause NPE.
     * @see org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer#updateInventory() 
     */
    @Unique(silent = true)
    @Override
    public void sendContainerToPlayer(@NotNull Container containerIn) {}
}
