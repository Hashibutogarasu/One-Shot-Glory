package com.karasu256.one_shot_glory.game;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.event.GameEventListener;
import com.karasu256.one_shot_glory.util.GameManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

/**
 * One-Shot-Glory プラグインのゲーム初期化と管理を行うユーティリティクラス
 * <p>
 * このクラスはゲームセッションの初期化、停止、およびイベントリスナーの
 * 登録解除など、ゲームの管理に関わる静的メソッドを提供します。
 * スコアボードの設定やチームの作成、ターゲットの生成なども行います。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class Initializer {
    /** ゲームイベントを監視するリスナー */
    private static GameEventListener gameEventListener;

    /**
     * Initializer クラスのデフォルトコンストラクタ
     * <p>
     * このクラスはユーティリティクラスであり、インスタンス化を防ぐために
     * プライベートコンストラクタを使用します。
     * </p>
     */
    private Initializer() {

    }

    /**
     * One-Shot-Gloryゲームを初期化するメソッド
     * <p>
     * 以下の処理を実行します：
     * </p>
     * <ul>
     * <li>既存のイベントリスナーを登録解除</li>
     * <li>プレイヤーチームを作成または取得し、全プレイヤーを追加</li>
     * <li>スコアボードの目標を設定</li>
     * <li>全プレイヤーのスコアを0にリセット</li>
     * <li>ワールドにターゲットを生成</li>
     * <li>新しいゲームイベントリスナーを登録</li>
     * </ul>
     * 
     * @param sender コマンド送信者（通知メッセージの送信先）
     * @return 初期化が成功した場合true
     */
    @SuppressWarnings("deprecation")
    public static boolean init(CommandSender sender) {
        sender.sendMessage("§aInitializing One Shot Glory...");

        // unregister the previous event listener
        var pluginManager = sender.getServer().getPluginManager();
        var plugin = One_Shot_Glory.getPlugin();

        if (plugin != null) {
            PlayerInteractEvent.getHandlerList().unregister(plugin);
        }

        var server = sender.getServer();

        // create a team which named "Members" and add all players to it
        var scoreboard = server.getScoreboardManager().getMainScoreboard();

        var team = scoreboard.getTeam("Members");

        if (team == null) {
            team = scoreboard.registerNewTeam("Members");
        }

        team.setColor(ChatColor.GREEN);

        Team finalTeam = team;
        var players = server.getOnlinePlayers();
        players.forEach(player -> finalTeam.addEntry(player.getName()));

        var playersInTeam = team.getEntries();

        // create or get a score board objective of "Score"
        var objective = scoreboard.getObjective("Score");

        if (objective == null) {
            objective = scoreboard.registerNewObjective("Score", "dummy", "Score");
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("§aScore");

        // show the score to tab list
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        // reset the score of all players
        Objective finalObjective = objective;
        playersInTeam.forEach(playerName -> {
            var player = server.getPlayer(playerName);
            if (player != null) {
                finalObjective.getScore(playerName).setScore(0);
            }
        });

        GameManager.spawnTarget(server.getWorld("world"));

        // register a event listener when the player killed by another player
        if (plugin != null) {
            gameEventListener = new GameEventListener(objective);
            pluginManager.registerEvents(gameEventListener, plugin);
        }

        sender.sendMessage("§aOne Shot Glory has been initialized!");

        return true;
    }

    /**
     * ゲームを停止するメソッド
     * <p>
     * ゲームイベントリスナーを登録解除してゲームを停止します。
     * </p>
     * 
     * @return 停止に成功した場合true、ゲームイベントリスナーがnullの場合false
     */
    public static boolean stop() {
        if (gameEventListener != null) {
            gameEventListener.unRegister();

            return true;
        }

        return false;
    }

    /**
     * イベントリスナーの登録を解除するメソッド
     * <p>
     * ゲームイベントリスナーをサーバーから登録解除します。
     * このメソッドはプラグインが無効化される際や、
     * 新しいゲームセッションを開始する前に呼び出されます。
     * </p>
     * 
     * @return 登録解除に成功した場合true、ゲームイベントリスナーがnullの場合false
     */
    public static boolean unRegister() {
        if (gameEventListener != null) {
            gameEventListener.unRegister();

            return true;
        }

        return false;
    }
}
