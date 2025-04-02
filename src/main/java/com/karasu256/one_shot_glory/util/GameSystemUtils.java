package com.karasu256.one_shot_glory.util;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.game.Initializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * ゲームシステムに関するユーティリティ機能を提供するクラス
 * <p>
 * このクラスはゲームの開始、停止、リセットなどの基本的な
 * ゲームシステム操作のためのメソッドを提供します。
 * </p>
 * 
 * @author Karasu256
 * @version 1.0
 */
public class GameSystemUtils {

    /**
     * ゲームを開始するメソッド
     * <p>
     * 設定ファイルの有効フラグを有効に設定し、ゲームの初期化処理を実行します。
     * </p>
     * 
     * @param sender コマンドを実行した送信者
     * @return ゲーム開始処理が成功した場合はtrue、失敗した場合はfalse
     */
    public static boolean startGame(CommandSender sender) {
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();
        
        One_Shot_Glory.config.set("enabled", true);
        plugin.saveConfig();
        
        sender.sendMessage(langManager.getMessage("commands.start.success", null));
        return Initializer.init(sender);
    }
    
    /**
     * ゲームを停止するメソッド
     * <p>
     * 設定ファイルの有効フラグを無効に設定し、イベントハンドラの登録を解除して
     * ゲームの停止処理を実行します。
     * </p>
     * 
     * @param sender コマンドを実行した送信者
     * @return ゲーム停止処理が成功した場合はtrue、失敗した場合はfalse
     */
    public static boolean stopGame(CommandSender sender) {
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();
        
        One_Shot_Glory.config.set("enabled", false);
        plugin.saveConfig();
        
        PlayerInteractEvent.getHandlerList().unregister(plugin);
        
        sender.sendMessage(langManager.getMessage("commands.stop.success", null));
        return Initializer.stop();
    }
}