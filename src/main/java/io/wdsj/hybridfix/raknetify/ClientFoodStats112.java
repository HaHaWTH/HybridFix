package io.wdsj.hybridfix.raknetify;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;

public final class ClientFoodStats112 extends FoodStats {
    public static ClientFoodStats112 from(FoodStats original) {
        NBTTagCompound compound = new NBTTagCompound();
        original.writeNBT(compound);
        ClientFoodStats112 copy = new ClientFoodStats112();
        copy.readNBT(compound);
        return copy;
    }

    @Override
    public void addStats(int foodLevel, float saturationModifier) {
        // Server-authoritative on a Raknetify-capable client.
    }

    @Override
    public void addStats(ItemFood food, ItemStack stack) {
        // Server-authoritative on a Raknetify-capable client.
    }
}
