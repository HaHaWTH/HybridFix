package io.wdsj.hybridfix.command;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.util.ItemStackUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.item.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Locale;

public class CommandHybridFix extends Command {
    public CommandHybridFix(String name) {
        super(name);
        this.description = "HybridFix commands";
        this.usageMessage = "/hybridfix dumpitem|version";
        setPermission("hybridfix.command.use");
    }

    public static class Reflection {
        public static final Method CraftItemStack$asNMSCopy;
        static {
            try {
                CraftItemStack$asNMSCopy = CraftItemStack.class.getDeclaredMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
                CraftItemStack$asNMSCopy.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "dumpitem":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                ItemStack itemInHand;
                try {
                    itemInHand = (ItemStack) Reflection.CraftItemStack$asNMSCopy.invoke(null, player.getInventory().getItemInMainHand());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (itemInHand.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "You are not holding any item.");
                    return true;
                }
                sender.sendMessage(ItemStackUtils.formatItemStackToPrettyString(itemInHand));
                TextComponent message = new TextComponent("[Click to insert give command]");
                message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ItemStackUtils.itemStackToGiveCommand(itemInHand)));
                sender.spigot().sendMessage(message);
                break;
            case "version":
                sender.sendMessage("This server is running HybridFix version " + HybridFix.VERSION);
                break;
        }

        return true;
    }

}
