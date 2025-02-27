package com.karasu256.one_shot_glory;

import com.karasu256.one_shot_glory.commands.MainCommand;
import com.karasu256.one_shot_glory.util.LanguageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public final class One_Shot_Glory extends JavaPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(One_Shot_Glory.class);
    public static @NotNull FileConfiguration config = new YamlConfiguration();
    private File configFile;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize config file
        configFile = new File(getDataFolder(), "config.yml");
        
        // Save default config if it doesn't exist
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        // Load config
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Initialize language manager
        languageManager = new LanguageManager(this);
        
        // Save config and register commands
        saveConfig();
        new MainCommand(this);
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public void onDisable() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        saveConfig();
    }

    @Override
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save the config file: {}", e.getMessage());
        }
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public static Plugin getPlugin() {
        return getPlugin(One_Shot_Glory.class);
    }
}
