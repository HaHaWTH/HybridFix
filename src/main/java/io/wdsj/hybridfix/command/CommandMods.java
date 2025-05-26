package io.wdsj.hybridfix.command;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (sender instanceof Player) {
            sender.spigot().sendMessage(makeComponent());
        } else {
            sender.sendMessage("Mods " + getModList());
        }
        return true;
    }

    public String getModList() {
        Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
        return "(" + mods.size() + "): " + mods.values().stream()
                .map(mod -> ChatColor.GREEN + mod.getName().replace(" ", ""))
                .collect(Collectors.joining(ChatColor.WHITE + ", "));
    }

    private TextComponent makeComponent() {
        Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
        TextComponent main = new TextComponent();
        int modsSize = mods.size();
        main.addExtra(new TextComponent("Mods (" + modsSize + "): "));
        int i = 1;
        for (ModContainer mod : mods.values()) {
            TextComponent modComponent = new TextComponent(mod.getName().replace(" ", ""));
            modComponent.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            TextComponent hoverComponent = new TextComponent();
            hoverComponent.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.WHITE + "Mod ID: " + mod.getModId() + "\nVersion: " + mod.getVersion()));
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverComponent});
            modComponent.setHoverEvent(hoverEvent);
            if (i < modsSize) {
                modComponent.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.WHITE + ", "));
            }
            main.addExtra(modComponent);
            i++;
        }
        return main;
    }
}
