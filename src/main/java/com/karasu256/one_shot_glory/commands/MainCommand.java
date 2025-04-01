package com.karasu256.one_shot_glory.commands;

import com.karasu256.one_shot_glory.commands.subcommands.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * プラグインのメインコマンドを処理するクラス
 * <p>
 * このクラスはOne-Shot-Gloryプラグインのコマンド構造を管理し、
 * サブコマンドの登録と実行のハンドリングを担当します。
 * ベースコマンドを通じてユーザーからの入力を各サブコマンドに委譲します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class MainCommand {
    /** プラグインのインスタンス */
    private final JavaPlugin plugin;
    /** コマンド実行を処理するベースコマンド */
    private final CommandExecutor baseCommand;

    /**
     * MainCommandのコンストラクタ
     * <p>
     * 与えられたプラグインインスタンスを保存し、
     * ベースコマンドを初期化して必要なサブコマンドを登録します。
     * </p>
     * 
     * @param plugin プラグインのインスタンス
     */
    public MainCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.baseCommand = new BaseCommand();
        registerCommands();
    }

    /**
     * サブコマンドの登録とプラグインコマンドの設定を行います。
     * <p>
     * ベースコマンドに各サブコマンドを登録し、
     * Bukkitのコマンドシステムにエグゼキュータとタブコンプリータを設定します。
     * plugin.ymlに登録されていないコマンドの場合は警告ログを出力します。
     * </p>
     */
    private void registerCommands() {
        if (baseCommand instanceof BaseCommand base) {
            base.registerSubCommand("help", new HelpSubCommand());
            base.registerSubCommand("reload", new ReloadSubCommand());
            base.registerSubCommand("config", new ConfigSubCommand());
            base.registerSubCommand("start", new StartSubCommand());
            base.registerSubCommand("stop", new StopSubCommand());
        }

        String commandName = "osg";
        if (plugin.getCommand(commandName) != null) {
            plugin.getCommand(commandName).setExecutor(baseCommand);
            if (baseCommand instanceof TabCompleter tabCompleter) {
                plugin.getCommand(commandName).setTabCompleter(tabCompleter);
            }
        } else {
            plugin.getLogger().warning("Command '" + commandName + "' not found in plugin.yml!");
        }
    }
}