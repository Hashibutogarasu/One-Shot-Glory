package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プラグインのリロード機能を提供するサブコマンドを実装するクラス
 * <p>
 * このクラスはリロードに関連する追加のサブコマンドを管理し、
 * 特定のコンポーネントのリロード機能へのアクセスを提供します。
 * 現在は設定のリロードサブコマンドをサポートしています。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class ReloadSubCommand implements SubCommand {
    /**
     * リロードサブコマンドで使用可能なサブコマンド集
     * <p>
     * キーはサブコマンド名（小文字）、値はSubCommandインターフェースの実装
     * </p>
     */
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    /**
     * ReloadSubCommandのコンストラクタ
     * <p>
     * 利用可能なリロードサブコマンドを初期化して登録します。
     * </p>
     */
    public ReloadSubCommand() {
        subCommands.put("config", new ReloadConfigSubCommand());
    }

    /**
     * リロードコマンドを実行するメソッド
     * <p>
     * このメソッドは以下の操作を行います：
     * </p>
     * <ul>
     *   <li>ユーザーの権限を確認</li>
     *   <li>追加の引数があれば、対応するサブコマンドへ処理を委譲</li>
     *   <li>引数がない場合は、利用可能なサブコマンドの一覧を表示</li>
     * </ul>
     * 
     * @param sender コマンドを実行した送信者
     * @param args コマンドの引数
     * @return コマンドの実行が成功した場合はtrue、失敗した場合はfalse
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();

        if (!sender.hasPermission("osg.reload")) {
            sender.sendMessage(langManager.getMessage("commands.no-permission", null));
            return true;
        }

        if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[1].toLowerCase());
            if (subCommand != null) {
                return subCommand.execute(sender, args);
            }
        }

        sender.sendMessage(langManager.getMessage("commands.reload.available", null));
        sender.sendMessage(langManager.getMessage("commands.reload.config-help", null));
        return true;
    }

    /**
     * リロードコマンドのタブ補完を提供するメソッド
     * <p>
     * 権限チェックを行い、適切な補完候補を返します。
     * 第2引数の場合は利用可能なサブコマンド一覧を返します。
     * さらに引数が続く場合は、選択されたサブコマンドのtabCompleteメソッドを呼び出します。
     * </p>
     * 
     * @param sender タブ補完を要求した送信者
     * @param args 現在入力されている引数
     * @return 補完候補の文字列リスト
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.reload")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            return new ArrayList<>(subCommands.keySet());
        }

        if (args.length > 2) {
            SubCommand subCommand = subCommands.get(args[1].toLowerCase());
            if (subCommand != null) {
                return subCommand.tabComplete(sender, args);
            }
        }

        return new ArrayList<>();
    }
}