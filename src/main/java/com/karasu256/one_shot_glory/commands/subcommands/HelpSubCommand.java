package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import com.karasu256.one_shot_glory.util.LanguageManager;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

/**
 * プラグインのヘルプ情報を表示するサブコマンドを実装するクラス
 * <p>
 * このクラスはOne-Shot-Gloryプラグインで利用可能な全コマンドの
 * 使用方法と説明を表示する機能を提供します。ユーザーがプラグインの
 * 機能を理解するための入り口として機能します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class HelpSubCommand implements SubCommand {
    /**
     * 言語マネージャーのインスタンス
     * <p>
     * 多言語メッセージの取得と表示に使用されます。
     * </p>
     */
    private final LanguageManager langManager;

    /**
     * HelpSubCommandのコンストラクタ
     * <p>
     * プラグインから言語マネージャーを取得して初期化します。
     * </p>
     */
    public HelpSubCommand() {
        this.langManager = One_Shot_Glory.getPlugin(One_Shot_Glory.class).getLanguageManager();
    }

    /**
     * ヘルプコマンドを実行するメソッド
     * <p>
     * プラグインで利用可能な全コマンドの説明と使用方法を
     * 送信者に表示します。各コマンドの説明は言語ファイルから
     * 取得されるため、多言語対応されています。
     * </p>
     * 
     * @param sender コマンドを実行した送信者
     * @param args コマンドの引数（このコマンドでは使用しません）
     * @return コマンドの実行が成功した場合はtrue
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(langManager.getMessage("commands.help.title", null));
        sender.sendMessage(langManager.getMessage("commands.help.help", null));
        sender.sendMessage(langManager.getMessage("commands.help.reload", null));
        sender.sendMessage(langManager.getMessage("commands.help.reload-config", null));
        sender.sendMessage(langManager.getMessage("commands.help.config", null));
        sender.sendMessage(langManager.getMessage("commands.help.start", null));
        sender.sendMessage(langManager.getMessage("commands.help.stop", null));
        return true;
    }

    /**
     * このサブコマンドのタブ補完を提供するメソッド
     * <p>
     * このコマンドには補完候補がないため、常に空のリストを返します。
     * </p>
     * 
     * @param sender タブ補完を要求した送信者
     * @param args 現在入力されている引数
     * @return 空の補完候補リスト
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}