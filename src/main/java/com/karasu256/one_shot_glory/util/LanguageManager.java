package com.karasu256.one_shot_glory.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * プラグインの多言語サポートを管理するクラス
 * <p>
 * このクラスは、プラグイン内のテキストメッセージを複数の言語で提供するための
 * 言語ファイルの読み込みと管理を行います。YAMLファイル形式の言語ファイルを使用し、
 * 指定された言語コードに基づいてメッセージを取得します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class LanguageManager {
    /** プラグインのインスタンス */
    private final JavaPlugin plugin;
    /** 言語コードとその設定ファイルのマッピング */
    private final Map<String, YamlConfiguration> languages;
    /** デフォルト言語のコード */
    private String defaultLanguage = "en_US";

    /**
     * LanguageManagerのコンストラクタ
     * <p>
     * インスタンス生成時に言語ファイルを読み込みます。
     * </p>
     * 
     * @param plugin このマネージャーを使用するJavaPluginのインスタンス
     */
    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        loadLanguages();
    }

    /**
     * 言語ファイルを読み込むメソッド
     * <p>
     * プラグインのlangディレクトリからすべての.ymlファイルを読み込み、
     * 言語マップに格納します。言語ディレクトリが存在しない場合は作成し、
     * デフォルトの言語ファイルを保存します。
     * </p>
     */
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

    /**
     * デフォルトの言語ファイルを保存するメソッド
     * <p>
     * 英語(en_US)と日本語(ja_JP)のデフォルト言語ファイルをプラグインのリソースから
     * langディレクトリに保存します。
     * </p>
     */
    private void saveDefaultLanguages() {
        saveResource("lang/en_US.yml");
        saveResource("lang/ja_JP.yml");
    }

    /**
     * プラグインのリソースをファイルとして保存するメソッド
     * <p>
     * 指定されたパスのリソースをプラグインのデータフォルダに保存します。
     * 保存先のディレクトリが存在しない場合は作成します。
     * </p>
     * 
     * @param resourcePath 保存するリソースのパス
     * @throws IllegalArgumentException リソースが見つからない場合、または保存できない場合
     */
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

    /**
     * 指定されたパスと言語コードに基づいてメッセージを取得するメソッド
     * <p>
     * 指定された言語コードの言語ファイルからメッセージを取得します。
     * メッセージが見つからない場合はデフォルト言語のメッセージを試み、
     * それも見つからない場合はパス自体を返します。
     * 取得したメッセージに対して色コードの変換と引数の置換を行います。
     * </p>
     * 
     * @param path メッセージのパス
     * @param langCode 言語コード。nullの場合はデフォルト言語が使用される
     * @param args 置換する引数の配列
     * @return 取得したメッセージ、または見つからない場合はパス自体
     */
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

    /**
     * デフォルト言語を設定するメソッド
     * <p>
     * 指定された言語コードが利用可能な場合、デフォルト言語として設定します。
     * </p>
     * 
     * @param langCode 設定するデフォルト言語コード
     */
    public void setDefaultLanguage(String langCode) {
        if (languages.containsKey(langCode)) {
            this.defaultLanguage = langCode;
        }
    }

    /**
     * 現在のデフォルト言語コードを取得するメソッド
     * 
     * @return デフォルト言語コード
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}