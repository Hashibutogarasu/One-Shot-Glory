package com.karasu256.one_shot_glory.commands;

import org.bukkit.command.CommandSender;
import java.util.List;

/**
 * One-Shot-Gloryプラグインのサブコマンド機能を定義するインターフェース
 * <p>
 * このインターフェースはコマンド実行とタブ補完の機能を提供します。
 * プラグインの各サブコマンドはこのインターフェースを実装する必要があります。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public interface SubCommand {
    
    /**
     * サブコマンドを実行するメソッド
     * <p>
     * コマンド送信者および引数を受け取り、適切な処理を実行します。
     * </p>
     * 
     * @param sender コマンドを実行した送信者
     * @param args コマンドの引数
     * @return コマンドの実行が成功した場合はtrue、失敗した場合はfalse
     */
    boolean execute(CommandSender sender, String[] args);
    
    /**
     * サブコマンドのタブ補完を提供するメソッド
     * <p>
     * 現在の入力状態に基づいて、コマンドの補完候補リストを返します。
     * </p>
     * 
     * @param sender タブ補完を要求した送信者
     * @param args 現在入力されている引数
     * @return 補完候補の文字列リスト
     */
    List<String> tabComplete(CommandSender sender, String[] args);
}