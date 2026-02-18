package io.wdsj.hybridfix.mixin.api.bukkit.entity;

import io.wdsj.hybridfix.api.bukkit.HybridFixBukkitApi;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin {
    // @formatter:off
    @Shadow public abstract EntityPlayerMP getHandle();
    @Unique
    private final Player.Spigot hybridFix$fakePlayerSpigot = new Player.Spigot() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private final Set<Player> EMPTY_SET = new HashSet<>(0);
        public InetSocketAddress getRawAddress() {
            return null;
        }

        public boolean getCollidesWithEntities() {
            return false;
        }

        public void setCollidesWithEntities(boolean collides) {
        }

        public void respawn() {
        }

        public void playEffect(Location location, Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed, int particleCount, int radius) {
        }

        public String getLocale() {
            return "en_US";
        }

        public Set<Player> getHiddenPlayers() {
            return Collections.unmodifiableSet(EMPTY_SET);
        }

        public void sendMessage(BaseComponent component) {
        }

        public void sendMessage(BaseComponent... components) {
        }

        public void sendMessage(ChatMessageType position, BaseComponent component) {
        }

        public void sendMessage(ChatMessageType position, BaseComponent... components) {
        }
    };
    // @formatter:on

    @Inject(
            method = "spigot()Lorg/bukkit/entity/Player$Spigot;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void spigot(CallbackInfoReturnable<Player.Spigot> cir) {
        if (HybridFixBukkitApi.getApi().isFakePlayer((CraftPlayer) (Object) this)) {
            cir.setReturnValue(hybridFix$fakePlayerSpigot);
        }
    }

    @Inject(
            method = "sendHealthUpdate",
            at = @At("HEAD"),
            cancellable = true
    )
    public void sendHealthUpdate(CallbackInfo ci) {
        if (this.getHandle().connection == null) ci.cancel();
    }

    @Inject(
            method = "sendTitle(Ljava/lang/String;Ljava/lang/String;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void sendTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks, CallbackInfo ci) {
        if (this.getHandle().connection == null) ci.cancel();
    }

    @Inject(
            method = "spawnParticle(Lorg/bukkit/Particle;DDDIDDDDLjava/lang/Object;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void spawnParticle(CallbackInfo ci) {
        if (this.getHandle().connection == null) ci.cancel();
    }

    // SkinsRestorer fix (Why did SR invoke this?)
    @SuppressWarnings({"AddedMixinMembersNamePattern", "unused"})
    @Unique(silent = true)
    public void sendPacket(Packet<?> packet) {
        if (this.getHandle().connection != null) {
            this.getHandle().connection.sendPacket(packet);
        }
    }
}
