package io.wdsj.hybridfix.mixin.api.bukkit.event.block;

import io.wdsj.hybridfix.duck.api.bukkit.event.block.IActionInvoker;
import org.bukkit.event.block.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static org.bukkit.event.block.Action.*;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mixin(value = Action.class, remap = false)
public abstract class ActionMixin implements IActionInvoker {
    @Unique
    @Override
    public boolean isLeftClick() {
        return (Object) this == LEFT_CLICK_AIR || (Object) this == LEFT_CLICK_BLOCK;
    }

    @Unique
    @Override
    public boolean isRightClick() {
        return (Object) this == RIGHT_CLICK_AIR || (Object) this == RIGHT_CLICK_BLOCK;
    }
}
