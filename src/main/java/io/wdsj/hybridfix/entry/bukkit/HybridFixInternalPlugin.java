package io.wdsj.hybridfix.entry.bukkit;

import io.wdsj.hybridfix.HybridFix;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@SuppressWarnings("all")
public class HybridFixInternalPlugin extends PluginBase {
    private boolean enabled = true;
    private PluginLoader loader;
    private static final HybridFixInternalPlugin INSTANCE = new HybridFixInternalPlugin();
    public static HybridFixInternalPlugin getInstance() {
        return INSTANCE;
    }
    private final String pluginName;
    private final File dataFolder;
    private final PluginLogger logger;
    private final PluginDescriptionFile pdf;

    public HybridFixInternalPlugin() {
        this.pluginName = "HybridFix";
        this.pdf = new PluginDescriptionFile(pluginName, HybridFix.VERSION, "hybridfix");
        this.dataFolder = new File("plugins", pluginName);
        this.logger = new PluginLogger(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return pdf;
    }

    @Override
    public FileConfiguration getConfig() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public InputStream getResource(String filename) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void saveConfig() {
    }

    @Override
    public void saveDefaultConfig() {
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
    }

    @Override
    public void reloadConfig() {
    }

    @Override
    public PluginLogger getLogger() {
        return logger;
    }

    @Override
    public PluginLoader getPluginLoader() {
        if (loader == null) {
            //noinspection deprecation
            loader = new JavaPluginLoader(Bukkit.getServer());
        }
        return loader;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean canNag) {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
