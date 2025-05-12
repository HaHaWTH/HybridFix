package io.wdsj.hybridfix.command;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.stream.Collectors;

public class CommandMods extends Command {

    public CommandMods(String name) {
        super(name);
        this.description = "HybridFix Mods";
        this.usageMessage = "/mods";
        setPermission("hybridfix.command.mods.use");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        sender.sendMessage("Mods " + getModList());
        return true;
    }

    public String getModList() {
        Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
        return "(" + mods.size() + "): " + mods.values().stream()
                .map(mod -> ChatColor.GREEN + mod.getName().replace(" ", ""))
                .collect(Collectors.joining(ChatColor.WHITE + ", "));
    }
}
