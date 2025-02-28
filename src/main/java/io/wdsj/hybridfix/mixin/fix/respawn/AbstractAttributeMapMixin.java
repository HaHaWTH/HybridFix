package io.wdsj.hybridfix.mixin.fix.respawn;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(AbstractAttributeMap.class)
public abstract class AbstractAttributeMapMixin {
    @Shadow @Final protected Map<String, IAttributeInstance> attributesByName;

    @Shadow public abstract IAttributeInstance getAttributeInstance(IAttribute attribute);

    @WrapMethod(
            method = "registerAttribute"
    )
    public IAttributeInstance wrapRegisterAttribute(IAttribute attribute, Operation<IAttributeInstance> original) {
        if (attributesByName.containsKey(attribute.getName())) {
            return getAttributeInstance(attribute);
        }
        return original.call(attribute);
    }

    @ModifyExpressionValue(
            method = "registerAttribute",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"
            )
    )
    public boolean modifyRegisterAttribute(boolean original, IAttribute attribute) {
        return false; // We have already checked in wrap method above
    }
}
