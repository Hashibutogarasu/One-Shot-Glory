package com.karasu256.one_shot_glory.util;

import org.bukkit.World;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * ゲームのメカニクスを管理するユーティリティクラス
 * <p>
 * このクラスは、ゲームのターゲットの生成やArmorStandの操作など、
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
     * 指定されたプレイヤーにターゲットを生成するメソッド
     * <p>
     * ランダムなバフを持つターゲットをプレイヤーに生成し、乗せます。
     * </p>
     * 
     * @param world  ターゲットを生成するワールド
     * @param player ターゲットを生成する対象のプレイヤー
     */
    public static void spawnTarget(World world, Player player) {
        BuffSystem randomBuff = BuffSystem.getRandomBuff(player);
        var itemStack = randomBuff.getBuffType().getItemStack();

        // ArmorStandUtilsを使用してアーマースタンドを生成
        var entity = ArmorStandUtils.spawnArmorStand(world, player, itemStack);

        player.addPassenger(entity);
    }
}
