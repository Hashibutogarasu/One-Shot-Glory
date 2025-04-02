package com.karasu256.one_shot_glory.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

/**
 * One-Shot-Gloryのプレイヤーに関するユーティリティクラス
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class OSGPlayerUtils {
    /** One-Shot-Gloryのシステムが有効なプレイヤーのリスト */
    private static List<Player> enabledList = new ArrayList<>();

    /**
     * 特定のプレイヤーのみにこのプラグインのシステムを有効化させます。
     * 
     * @param player 有効にするプレイヤー
     */
    public static void enableOneShotSystemForPlayer(Player player) {
        if (!enabledList.contains(player)) {
            enabledList.add(player);
        }
    }

    /**
     * 特定のプレイヤーのみにこのプラグインのシステムを無効化させます。
     * 
     * @param player 無効にするプレイヤー
     */
    public static void disableOneShotSystemForPlayer(Player player) {
        if (enabledList.contains(player)) {
            enabledList.remove(player);
        }
    }

    /**
     * 有効化されているプレイヤーのリストを取得します。
     * 
     * @return プレイヤーのリスト
     */
    public static List<Player> getEnabledList() {
        return enabledList;
    }
}
