package com.karasu256.one_shot_glory.util;

import org.bukkit.World;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * ゲームのメカニクスを管理するユーティリティクラス
 * <p>
 * このクラスは、ゲームのターゲットの生成やItemFrameの操作など、
 * ゲーム内のさまざまなメカニクスを管理するための静的メソッドを提供します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class GameManager {

    /**
     * GameManagerのデフォルトコンストラクタ
     * <p>
     * 新しいGameManagerインスタンスを初期化します。
     * </p>
     */
    private GameManager() {

    }

    /**
     * 指定されたワールド内のすべてのプレイヤーにターゲットを生成するメソッド
     * <p>
     * ワールド内のすべてのプレイヤーに対して個別にターゲットを生成します。
     * </p>
     * 
     * @param world ターゲットを生成するワールド
     */
    public static void spawnTarget(World world) {
        List<Player> players = world.getPlayers();

        for (var player : players) {
            spawnTarget(world, player);
        }
    }

    /**
     * プレイヤーのターゲットを生成するメソッド
     * <p>
     * プレイヤーの現在位置にターゲットを生成します。
     * </p>
     * 
     * @param player ターゲットの所有者となるプレイヤー
     */
    public static void spawnTarget(Player player) {
        spawnTarget(player.getWorld(), player);
    }

    /**
     * プレイヤーのターゲットを生成するメソッド
     * 
     * @param world  ターゲットを生成するワールド
     * @param player ターゲットの所有者となるプレイヤー
     */
    public static void spawnTarget(World world, Player player) {
        if (player == null) {
            return;
        }

        // プレイヤーが有効でない場合は何もしない
        if (!OSGPlayerUtils.isPlayerEnabled(player)) {
            return;
        }

        // プレイヤーの現在位置を取得
        Location location = player.getLocation();
        location.setY(location.getY() + 2);

        ItemStack displayItem = BuffSystem.getRandomBuff(player).getBuffType().getItemStack();

        // アイテムフレームを生成
        ItemFrame itemFrame = ItemFrameUtils.spawnItemFrame(world, player, displayItem);
        itemFrame.setItem(displayItem);
    }
}
