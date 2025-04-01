package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

/**
 * プラグインの設定をリロードするサブコマンドを実装するクラス
 * <p>
 * このクラスはプラグインの設定ファイルを再読み込みし、
 * 最新の設定を反映させます。設定の変更後にプラグインの
 * 動作を更新したい場合に使用します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class ReloadConfigSubCommand implements SubCommand {

    /**
     * ReloadConfigSubCommandクラスのデフォルトコンストラクタ
     * <p>
     * このクラスサブコマンドの実装を初期化します。
     * </p>
     */
    public ReloadConfigSubCommand() {

    }

    /**
     * 設定リロードコマンドを実行するメソッド
     * <p>
     * このメソッドは以下の操作を行います：
     * </p>
     * <ul>
     * <li>ユーザーの権限を確認</li>
     * <li>プラグインの設定を再読み込み</li>
     * <li>設定を保存して反映</li>
     * <li>操作結果をユーザーに通知</li>
     * </ul>
     * <p>
     * エラーが発生した場合は例外をキャッチし、ユーザーにエラーメッセージを表示します。
     * </p>
     * 
     * @param sender コマンドを実行した送信者
     * @param args   コマンドの引数（このコマンドでは使用しません）
     * @return コマンドの実行が成功した場合はtrue
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.reload")) {
            sender.sendMessage(One_Shot_Glory.getPlugin(One_Shot_Glory.class).getLanguageManager()
                    .getMessage("commands.no-permission", null));
            return true;
        }

        try {
            One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
            One_Shot_Glory.config = plugin.getConfig();
            plugin.loadConfig();
            plugin.saveConfig();
            plugin.reloadConfig();
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.config.success", null));
        } catch (Exception e) {
            One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.config.error", null)
                    .replace("{error}", e.getMessage()));
        }
        return true;
    }

    /**
     * このサブコマンドのタブ補完を提供するメソッド
     * <p>
     * このコマンドには補完候補がないため、常に空のリストを返します。
     * </p>
     * 
     * @param sender タブ補完を要求した送信者
     * @param args   現在入力されている引数
     * @return 空の補完候補リスト
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}