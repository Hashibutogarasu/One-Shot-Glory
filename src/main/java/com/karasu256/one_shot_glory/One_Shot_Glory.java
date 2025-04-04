package com.karasu256.one_shot_glory;

import com.karasu256.one_shot_glory.commands.MainCommand;
import com.karasu256.one_shot_glory.util.ItemFrameUtils;
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

/**
 * One-Shot-Gloryプラグインのメインクラス
 * <p>
 * このクラスはOne-Shot-Gloryプラグインのエントリーポイントとなり、
 * プラグインの初期化、設定ファイルの管理、および各種機能の制御を行います。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 0.1.0
 */
public final class One_Shot_Glory extends JavaPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(One_Shot_Glory.class);
    /** プラグインの設定情報を保持する設定オブジェクト */
    public static @NotNull FileConfiguration config = new YamlConfiguration();
    /** 設定ファイルへの参照 */
    private File configFile;
    /** 言語管理を行うマネージャー */
    private LanguageManager languageManager;

    /**
     * プラグインが有効化された際に呼び出されるメソッド
     * <p>
     * データフォルダの作成、設定ファイルの初期化、言語マネージャーの初期化、
     * およびコマンドの登録などの初期化処理を行います。
     * </p>
     */
    @Override
    public void onEnable() {
        // プラグインのデータフォルダが存在しない場合は作成
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // 設定ファイルの初期化
        configFile = new File(getDataFolder(), "config.yml");

        // 設定ファイルが存在しない場合はデフォルト設定を保存
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        // 設定の読み込み
        config = YamlConfiguration.loadConfiguration(configFile);

        // 言語マネージャーの初期化
        languageManager = new LanguageManager(this);

        // 設定の保存とコマンドの登録
        saveConfig();
        new MainCommand(this);
    }

    /**
     * 設定ファイルを再読み込みするメソッド
     * <p>
     * 設定ファイルの内容を最新の状態に更新します。
     * </p>
     */
    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * プラグインが無効化された際に呼び出されるメソッド
     * <p>
     * イベントリスナーの登録解除や設定ファイルの保存など、
     * プラグインのシャットダウン処理を行います。
     * </p>
     */
    @Override
    public void onDisable() {
        ItemFrameUtils.removeAllItemFrames();
        PlayerInteractEvent.getHandlerList().unregister(this);
        saveConfig();
    }

    /**
     * 設定ファイルを保存するメソッド
     * <p>
     * 現在のメモリ上の設定内容をファイルに保存します。
     * 保存に失敗した場合はエラーログを出力します。
     * </p>
     */
    @Override
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            LOGGER.error("設定ファイルの保存に失敗しました: {}", e.getMessage());
        }
    }

    /**
     * 言語マネージャーを取得するメソッド
     * 
     * @return 言語マネージャーのインスタンス
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * プラグインのインスタンスを取得するためのユーティリティメソッド
     * 
     * @return プラグインのインスタンス
     */
    public static Plugin getPlugin() {
        return getPlugin(One_Shot_Glory.class);
    }
}
