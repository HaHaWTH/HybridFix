package io.wdsj.hybridfix.mixin.api.bukkit.event.block;

import org.bukkit.event.block.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static org.bukkit.event.block.Action.*;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mixin(value = Action.class, remap = false)
public abstract class ActionMixin {
    /**
     * Gets whether this action is a result of a left click.
     *
     * @return Whether it's a left click
     */
    @Unique
    public boolean isLeftClick() {
        return (Object) this == LEFT_CLICK_AIR || (Object) this == LEFT_CLICK_BLOCK;
    }

    /**
     * Gets whether this action is a result of a right click.
     *
     * @return Whether it's a right click
     */
    @Unique
    public boolean isRightClick() {
        return (Object) this == RIGHT_CLICK_AIR || (Object) this == RIGHT_CLICK_BLOCK;
    }
}
