package io.wdsj.hybridfix.api.bukkit.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity is attacked, even the entity damaged is in invulnerable time.
 * This event is a bukkit-side equivalent of Forge's {@link net.minecraftforge.event.entity.living.LivingAttackEvent}.
 * Fired earlier than the {@link org.bukkit.event.entity.EntityDamageEvent}, at this point the damage has not been calculated yet.
 */
@SuppressWarnings("unused")
public class EntityAttackEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final double damage;
    private final String damageType;

    @ApiStatus.Internal
    public EntityAttackEvent(Entity victim, double damage, String damageType) {
        super(victim);
        this.damage = damage;
        this.damageType = damageType;
    }

    /**
     * Get the <b>base</b> damage in the attack.
     *
     * @return the damage
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Get the damage type of the attack.
     *
     * @return the damage type
     */
    @NotNull
    public String getDamageType() {
        return damageType;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
