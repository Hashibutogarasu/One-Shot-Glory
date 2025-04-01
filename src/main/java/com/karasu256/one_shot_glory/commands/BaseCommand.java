package com.karasu256.one_shot_glory.commands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One-Shot-Gloryプラグインのコマンド実行を管理するベースクラス
 * <p>
 * このクラスは登録されたサブコマンドへの仲介役として機能し、
 * コマンド実行とタブ補完機能を提供します。各サブコマンドは
 * このクラスに登録され、適切な処理へ委譲されます。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class BaseCommand implements CommandExecutor, TabCompleter {
    /**
     * サブコマンド名と実装の対応マップ
     * <p>
     * キーはサブコマンド名（小文字）、値はSubCommandインターフェースの実装
     * </p>
     */
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    /**
     * BaseCommandのデフォルトコンストラクタ
     * <p>
     * 新しいBaseCommandインスタンスを初期化します。
     * サブコマンド用の空のマップを作成します。
     * </p>
     */
    public BaseCommand() {

    }

    /**
     * サブコマンドを登録するメソッド
     * <p>
     * 指定された名前でサブコマンドを登録します。名前は内部的に小文字に変換されます。
     * </p>
     *
     * @param name       サブコマンドの名前
     * @param subCommand サブコマンドの実装
     */
    public void registerSubCommand(String name, SubCommand subCommand) {
        subCommands.put(name.toLowerCase(), subCommand);
    }

    /**
     * コマンド実行時に呼び出されるメソッド
     * <p>
     * 適切なサブコマンドを検索し、そのサブコマンドのexecuteメソッドを呼び出します。
     * サブコマンドが指定されていない場合や、存在しないサブコマンドの場合はエラーメッセージを表示します。
     * </p>
     *
     * @param sender  コマンドを実行した送信者
     * @param command 実行されたコマンド
     * @param label   使用されたコマンドラベル
     * @param args    コマンドの引数
     * @return コマンドが正常に実行された場合はtrue
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {
        if (args.length == 0) {
            sender.sendMessage(One_Shot_Glory.getPlugin(One_Shot_Glory.class).getLanguageManager()
                    .getMessage("commands.specify-subcommand", null));
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage(One_Shot_Glory.getPlugin(One_Shot_Glory.class).getLanguageManager()
                    .getMessage("commands.unknown-subcommand", null));
            return true;
        }

        return subCommand.execute(sender, args);
    }

    /**
     * タブ補完時に呼び出されるメソッド
     * <p>
     * 第一引数の場合は登録済みのサブコマンド一覧を返します。
     * サブコマンドが指定されている場合は、そのサブコマンドのtabCompleteメソッドを呼び出します。
     * </p>
     *
     * @param sender  タブ補完を要求した送信者
     * @param command 実行中のコマンド
     * @param alias   使用されたエイリアス
     * @param args    現在入力されている引数
     * @return 補完候補の文字列リスト
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream().toList();
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            return subCommand.tabComplete(sender, args);
        }

        return List.of();
    }
}