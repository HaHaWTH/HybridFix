package io.wdsj.hybridfix.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.ItemStackUtils;
import io.wdsj.hybridfix.util.SpigotReflectionUtils;
import io.wdsj.hybridfix.util.Updater;
import io.wdsj.hybridfix.util.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandHybridFix extends Command {
    private static final Cache<String, String> versionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.HOURS)
            .maximumSize(1L)
            .build();
    public CommandHybridFix(String name) {
        super(name);
        this.description = "HybridFix commands";
        this.usageMessage = "/hybridfix dumpitem|version";
        setPermission("hybridfix.command.use");
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
                ItemStack itemInHand = SpigotReflectionUtils.CraftItemStack_asNMSCopy(player.getInventory().getItemInMainHand());
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
                sender.sendMessage("This server is running HybridFix version " + HybridFix.VERSION + " (" + Bukkit.getVersion() + ")");
                if (Settings.checkForUpdates) {
                    sender.sendMessage(ChatColor.ITALIC + "Checking version, please wait...");
                    String cachedLatestVersion = versionCache.getIfPresent(HybridFix.VERSION);
                    if (cachedLatestVersion != null) {
                        if (!cachedLatestVersion.equals(HybridFix.VERSION)) {
                            sender.sendMessage(ChatColor.YELLOW + "* There is an update available: " + Updater.getLatestVersion() + ", you're on: " + HybridFix.VERSION + ".");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "* You are running the latest version.");
                        }
                    } else {
                        CompletableFuture.supplyAsync(Updater::isUpdateAvailable, Utils.ioWorker())
                                .thenAccept(
                                        isUpdateAvailable -> {
                                            if (isUpdateAvailable) {
                                                sender.sendMessage(ChatColor.YELLOW + "* There is an update available: " + Updater.getLatestVersion() + ", you're on: " + HybridFix.VERSION + ".");
                                            } else {
                                                if (!Updater.isErred()) {
                                                    sender.sendMessage(ChatColor.GREEN + "* You are running the latest version.");
                                                } else {
                                                    sender.sendMessage(ChatColor.RED + "* Error obtaining version information.");
                                                }
                                            }
                                            versionCache.put(HybridFix.VERSION, Updater.getLatestVersion());
                                        }
                                );
                    }
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
                return false;
        }
        return true;
    }

}
