package io.wdsj.hybridfix.util;

import io.wdsj.hybridfix.HybridFix;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FormatUtils {
    public static String itemStackToGiveCommand(ItemStack nmsItemStack) {
        ResourceLocation itemRl = Item.REGISTRY.getNameForObject(nmsItemStack.getItem());
        String itemNamespacedId = itemRl != null ? itemRl.toString() : "";
        if (itemNamespacedId.isEmpty()) {
            HybridFix.LOGGER.warn("Failed to parse item {} to command", nmsItemStack.toString());
            return "Failed to parse item to command";
        }

        int count = nmsItemStack.getCount();

        NBTTagCompound tag = nmsItemStack.getTagCompound();
        String nbtData = tag != null && !tag.isEmpty() ? tag.toString() : "";

        StringBuilder command = new StringBuilder();
        command.append("/minecraft:give ").append("@s").append(" ");
        command.append(itemNamespacedId).append(" ").append(count);
        command.append(" ").append(nmsItemStack.getItemDamage());

        if (!nbtData.isEmpty()) {
            command.append(" ").append(nbtData);
        }

        return command.toString();
    }

    // Adapted from CraftTweaker, licensed under MIT
    public static String formatItemStackToPrettyString(ItemStack itemStack) {
        StringBuilder fullBuilder = new StringBuilder();
        ResourceLocation rl = Item.REGISTRY.getNameForObject(itemStack.getItem());
        fullBuilder.append("\u00A7e").append(rl != null ? rl.toString() : "unknown:unknown").append("\u00A7r, ");
        fullBuilder.append("\u00A7ddamage\u00A7r:\u00A7b ").append(itemStack.getItemDamage()).append("\u00A7r, ");
        NBTTagCompound tagCompound = itemStack.getTagCompound();

        return fullBuilder.append(formatNBT(tagCompound)).toString();
    }

    private static void addNewLine(StringBuilder s, int indent) {
        s.append("\n\u00A7e\u251c");
        for(int j = 0; j < indent; j++) {
            s.append("\u00A7e    ");
        }
    }

    private static void addNewLineNoColor(StringBuilder s, int indent) {
        s.append("\n\u251c");
        for(int j = 0; j < indent; j++) {
            s.append("    ");
        }
    }

    public static String formatNBT(NBTTagCompound tagCompound) {
        if (tagCompound == null || tagCompound.isEmpty()) {
            return "\u00A7eNo NBT data";
        }

        String nbtString = tagCompound.toString();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderNonColor = new StringBuilder();
        int currentIndent = 0;

        boolean inQuotes = false;
        boolean isInValue = false;

        stringBuilder.append("\u00A7e\u251c");
        stringBuilderNonColor.append("\n\u251c");


        for (int i = 0; i < nbtString.length(); i++) {
            char[] cArray = new char[1];
            nbtString.getChars(i, i + 1, cArray, 0);
            char c = cArray[0];

            switch (c) {
                case '"':
                    inQuotes = !inQuotes;
                    stringBuilder.append("\u00A73" + '"');
                    stringBuilderNonColor.append(c);

                    break;
                case '{':
                case '[':
                    if (!inQuotes) {
                        currentIndent++;
                        isInValue = false;

                        stringBuilder.append("\u00A72").append(c);
                        addNewLine(stringBuilder, currentIndent);

                        stringBuilderNonColor.append(c);
                        addNewLineNoColor(stringBuilderNonColor, currentIndent);
                    } else {
                        stringBuilder.append("\u00A7b").append(c);
                        stringBuilderNonColor.append(c);

                    }
                    break;
                case '}':
                case ']':
                    if (!inQuotes) {
                        currentIndent--;
                        isInValue = false;
                        addNewLine(stringBuilder, currentIndent);
                        stringBuilder.append("\u00A72").append(c);

                        addNewLineNoColor(stringBuilderNonColor, currentIndent);
                        stringBuilderNonColor.append(c);
                    } else {
                        stringBuilder.append("\u00A7b").append(c);
                        stringBuilderNonColor.append(c);

                    }
                    break;
                case ',':
                    if (!inQuotes) {
                        isInValue = false;
                        stringBuilder.append("\u00A72,");
                        addNewLine(stringBuilder, currentIndent);

                        stringBuilderNonColor.append(c);
                        addNewLineNoColor(stringBuilderNonColor, currentIndent);

                    } else {
                        stringBuilder.append("\u00A7b,");
                        stringBuilderNonColor.append(c);

                    }
                    break;
                case ':':
                    if (!inQuotes) {
                        isInValue = true;
                        stringBuilder.append("\u00A72").append(c);
                        stringBuilderNonColor.append(c);

                    } else {
                        stringBuilder.append("\u00A7b").append(c);
                        stringBuilderNonColor.append(c);

                    }
                    break;
                default:
                    if (inQuotes) {
                        stringBuilder.append("\u00A7b").append(c);
                        stringBuilderNonColor.append(c);

                    } else if (isInValue) {
                        stringBuilder.append("\u00A7b").append(c);
                        stringBuilderNonColor.append(c);

                    } else {
                        stringBuilder.append(c);
                        stringBuilderNonColor.append(c);
                    }
            }
        }
        return stringBuilder.toString();
    }
}
