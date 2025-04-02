package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import com.karasu256.one_shot_glory.util.GameSystemUtils;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

/**
 * プラグインの機能を停止するサブコマンドを実装するクラス
 * <p>
 * このクラスはプラグインの全機能を無効化し、設定を更新して
 * イベントハンドラの登録を解除します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class StopSubCommand implements SubCommand {

    /**
     * StopSubCommandクラスのデフォルトコンストラクタ
     * <p>
     * このクラスサブコマンドの実装を初期化します。
     * </p>
     */
    public StopSubCommand() {

    }

    /**
     * プラグインの機能を停止するコマンドを実行します
     * <p>
     * このメソッドは以下の操作を行います：
     * <ul>
     * <li>ユーザーの権限を確認</li>
     * <li>GameSystemUtilsのstopGameメソッドを呼び出し</li>
     * </ul>
     * 
     * @param sender コマンドを実行した送信者
     * @param args   コマンドの引数（このコマンドでは使用しません）
     * @return コマンドの実行が成功した場合はtrue、失敗した場合はfalse
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();

        if (!sender.hasPermission("osg.stop")) {
            sender.sendMessage(langManager.getMessage("commands.no-permission", null));
            return true;
        }

        return GameSystemUtils.stopGame(sender);
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