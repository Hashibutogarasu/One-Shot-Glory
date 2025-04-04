package com.karasu256.one_shot_glory.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

/**
 * One-Shot-Gloryのプレイヤーに関するユーティリティクラス
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class OSGPlayerUtils {
    /**
     * OSGPlayerUtilsのデフォルトコンストラクタ
     */
    public OSGPlayerUtils(){

    }

    /**
     * 特定のプレイヤーのみにこのプラグインのシステムを有効化させます。
     * 
     * @param player 有効にするプレイヤー
     */
    public static void enableOneShotSystemForPlayer(Player player) {
        Objective objective = getEnabledPlayersObjective();
        if (objective != null) {
            objective.getScore(player.getName()).setScore(1);
        }
    }

    /**
     * 特定のプレイヤーのみにこのプラグインのシステムを無効化させます。
     * 
     * @param player 無効にするプレイヤー
     */
    public static void disableOneShotSystemForPlayer(Player player) {
        Objective objective = getEnabledPlayersObjective();
        if (objective != null) {
            objective.getScore(player.getName()).setScore(0);
            ItemFrameUtils.removePlayerItemFrame(player);
        }
    }

    /**
     * プレイヤーがシステムで有効になっているかどうかを確認します。
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーが有効な場合はtrue
     */
    public static boolean isPlayerEnabled(Player player) {
        Objective objective = getEnabledPlayersObjective();
        if (objective != null) {
            return objective.getScore(player.getName()).getScore() > 0;
        }
        return false;
    }

    /**
     * 有効化されているプレイヤーのリストを取得します。
     * 
     * @return プレイヤーのリスト
     */
    public static List<Player> getEnabledList() {
        Objective objective = getEnabledPlayersObjective();
        if (objective != null) {
            return Bukkit.getOnlinePlayers().stream()
                .filter(player -> objective.getScore(player.getName()).getScore() > 0)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * osg_enabled_playersスコアボードオブジェクトを取得します。
     * 存在しない場合は作成します。
     *
     * @return スコアボードオブジェクト
     */
    private static Objective getEnabledPlayersObjective() {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var objective = scoreboard.getObjective("osg_enabled_players");
        
        if (objective == null) {
            objective = scoreboard.registerNewObjective("osg_enabled_players", "dummy", "OSG Enabled Players");
        }
        
        return objective;
    }
}
