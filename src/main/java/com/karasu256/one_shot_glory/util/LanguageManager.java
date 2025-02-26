package com.karasu256.one_shot_glory.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final JavaPlugin plugin;
    private final Map<String, YamlConfiguration> languages;
    private String defaultLanguage = "en_US";

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        loadLanguages();
    }

    private void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            saveDefaultLanguages();
        }

        // Load all .yml files from the lang folder
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                var config = YamlConfiguration.loadConfiguration(file);
                var code = config.getString("langCode", langCode);
                plugin.getLogger().info("Loaded language: " + code);

                languages.put(langCode, config);
            }
        }
    }

    private void saveDefaultLanguages() {
        saveResource("lang/en_US.yml");
        saveResource("lang/ja_JP.yml");
    }

    private void saveResource(String resourcePath) {
        if (resourcePath != null && !resourcePath.isEmpty()) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
            }

            File outFile = new File(plugin.getDataFolder(), resourcePath);
            int lastIndex = resourcePath.lastIndexOf('/');
            File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            try {
                if (!outFile.exists()) {
                    plugin.saveResource(resourcePath, false);
                }
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Could not save " + outFile.getName() + " to " + outFile + ": " + ex.getMessage());
            }
        }
    }

    public String getMessage(@NotNull String path, String langCode, Object... args) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // If langCode is null, use default language
        if (langCode == null) {
            String configLangCode = plugin.getConfig().getString("language.default");
            langCode = configLangCode != null ? configLangCode : defaultLanguage;
        }

        YamlConfiguration lang = languages.getOrDefault(langCode, languages.get(defaultLanguage));
        String message = null;

        // Try getting message from specified language
        if (lang != null) {
            message = lang.getString(path);
        }

        // Try fallback to default language if necessary
        if (message == null && !langCode.equals(defaultLanguage)) {
            YamlConfiguration defaultLang = languages.get(defaultLanguage);
            if (defaultLang != null) {
                message = defaultLang.getString(path);
            }
        }

        // If still no message found, return the path as fallback
        if (message == null) {
            plugin.getLogger().warning("Missing translation for path: " + path + " in language: " + langCode);
            return path;
        }

        // Replace color codes
        message = message.replace("&", "§");

        // Replace arguments if any
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }

        return message;
    }

    public void setDefaultLanguage(String langCode) {
        if (languages.containsKey(langCode)) {
            this.defaultLanguage = langCode;
        }
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}