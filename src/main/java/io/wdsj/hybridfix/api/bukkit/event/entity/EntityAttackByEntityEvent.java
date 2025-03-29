package io.wdsj.hybridfix.api.bukkit.event.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when an entity is being attacked by another entity.
 */
@SuppressWarnings("unused")
public class EntityAttackByEntityEvent extends EntityAttackEvent {
    private final Entity damager;

    @ApiStatus.Internal
    public EntityAttackByEntityEvent(Entity victim, double damage, Entity damager, String damageType) {
        super(victim, damage, damageType);
        this.damager = damager;
    }

    @NotNull
    public Entity getDamager() {
        return damager;
    }
}
