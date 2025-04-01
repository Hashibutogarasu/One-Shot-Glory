package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * プラグインの設定を参照・変更するサブコマンドを実装するクラス
 * <p>
 * このクラスはOne-Shot-Gloryプラグインの設定値を動的に確認したり
 * 変更したりする機能を提供します。設定プロパティの値を取得したり、
 * 新しい値を設定することができます。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class ConfigSubCommand implements SubCommand {
    
    /**
     * ReloadConfigSubCommandクラスのデフォルトコンストラクタ
     * <p>
     * このクラスサブコマンドの実装を初期化します。
     * </p>
     */
    public ConfigSubCommand() {

    }

    /**
     * 設定管理コマンドを実行するメソッド
     * <p>
     * このメソッドは以下の操作を行います：
     * </p>
     * <ul>
     *   <li>ユーザーの権限を確認</li>
     *   <li>引数が1つの場合は、指定されたプロパティの現在の値を表示</li>
     *   <li>引数が2つの場合は、指定されたプロパティに新しい値を設定</li>
     *   <li>値のタイプ（整数、真偽値、文字列）を自動判別</li>
     * </ul>
     * 
     * @param sender コマンドを実行した送信者
     * @param args コマンドの引数（プロパティ名と設定値）
     * @return コマンドの実行が成功した場合はtrue
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();

        if (!sender.hasPermission("osg.config")) {
            sender.sendMessage(langManager.getMessage("commands.no-permission", null));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langManager.getMessage("commands.config.usage", null));
            return true;
        }

        String property = args[1];
        Configuration config = plugin.getConfig();
        
        if (args.length == 2) {
            Object value = config.get(property);
            if (value == null) {
                sender.sendMessage(langManager.getMessage("commands.config.not-found", null)
                    .replace("{property}", property));
            } else {
                sender.sendMessage(langManager.getMessage("commands.config.current-value", null)
                    .replace("{property}", property)
                    .replace("{value}", value.toString()));
            }
            return true;
        }

        String value = args[2];
        try {
            int intValue = Integer.parseInt(value);
            config.set(property, intValue);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                boolean boolValue = Boolean.parseBoolean(value);
                config.set(property, boolValue);
            } else {
                config.set(property, value);
            }
        }

        plugin.saveConfig();
        sender.sendMessage(langManager.getMessage("commands.config.set-value", null)
            .replace("{property}", property)
            .replace("{value}", value));
        return true;
    }

    /**
     * 設定コマンドのタブ補完を提供するメソッド
     * <p>
     * 2番目の引数を入力中の場合は、利用可能な設定キーのリストを提供します。
     * 権限がない場合や、他の引数位置では空のリストを返します。
     * </p>
     * 
     * @param sender タブ補完を要求した送信者
     * @param args 現在入力されている引数
     * @return 補完候補の文字列リスト
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.config")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
            return new ArrayList<>(plugin.getConfig().getKeys(true));
        }

        return new ArrayList<>();
    }
}