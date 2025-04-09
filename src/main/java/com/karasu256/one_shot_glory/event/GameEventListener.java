package com.karasu256.one_shot_glory.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.util.BuffSystem;
import com.karasu256.one_shot_glory.util.BuffType;
import com.karasu256.one_shot_glory.util.GameManager;
import com.karasu256.one_shot_glory.util.ItemFrameUtils;
import com.karasu256.one_shot_glory.util.OSGPlayerUtils;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import net.kyori.adventure.text.Component;

/**
 * One-Shot-Gloryゲームでのイベントを処理するリスナークラス
 * <p>
 * このクラスは、ゲームのメカニクスに関連するサーバーイベントをリッスンし、
 * 適切な処理を行います。矢の命中、エンティティのダメージ、プレイヤーの移動、
 * プレイヤーの死亡、リスポーンなどのイベントを処理します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class GameEventListener implements Listener {
    /** スコア管理のための目標オブジェクト */
    private final Objective objective;

    /**
     * アーマースタンドの当たり判定を更新するタスクのスケジューラーID
     */
    private int collisionCheckTaskId = -1;

    /** アーマースタンドのコリジョン状態を保持するマップ */
    private static final Map<UUID, Boolean> armorStandCollisionStates = new ConcurrentHashMap<>();

    /** プレイヤーのゲームモード履歴を管理するマップ */
    private static final Map<UUID, Deque<GameMode>> playerGameModeHistory = new ConcurrentHashMap<>();

    /**
     * GameEventListenerのコンストラクタ
     * 
     * @param objective スコアを記録するための目標オブジェクト
     */
    public GameEventListener(Objective objective) {
        this.objective = objective;
    }

    /**
     * このリスナーの登録を解除するメソッド
     * <p>
     * 関連するすべてのイベントハンドラの登録を解除し、
     * 実行中のタスクを停止します。
     * このメソッドは、ゲームが停止したときや、プラグインが無効化されるときに呼び出されます。
     * </p>
     */
    public void unRegister() {
        ProjectileHitEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerGameModeChangeEvent.getHandlerList().unregister(this);
        PlayerItemHeldEvent.getHandlerList().unregister(this);
        PlayerItemFrameChangeEvent.getHandlerList().unregister(this);

        // タスクを停止
        if (collisionCheckTaskId != -1) {
            One_Shot_Glory.getPlugin().getServer().getScheduler().cancelTask(collisionCheckTaskId);
            collisionCheckTaskId = -1;
        }

        // 状態管理マップをクリア
        armorStandCollisionStates.clear();
    }

    /**
     * アイテムフレームの当たり判定を設定するメソッド
     * 
     * @param itemFrame    対象のアイテムフレーム
     * @param hasCollision 当たり判定を有効にするかどうか
     */
    private void setItemFrameCollision(ItemFrame itemFrame, boolean hasCollision) {
        armorStandCollisionStates.put(itemFrame.getUniqueId(), hasCollision);
    }

    /**
     * プレイヤーのゲームモード履歴を更新し、変更があったかどうかを返すメソッド
     * 
     * @param player          対象のプレイヤー
     * @param currentGameMode 現在のゲームモード
     * @return ゲームモードが変更されたかどうか
     */
    private boolean updateGameModeHistory(Player player, GameMode currentGameMode) {
        UUID playerId = player.getUniqueId();
        Deque<GameMode> history = playerGameModeHistory.computeIfAbsent(playerId, k -> new ArrayDeque<>());

        // 履歴が空の場合は初期化
        if (history.isEmpty()) {
            history.addLast(currentGameMode);
            history.addLast(currentGameMode);
            return false;
        }

        // 最新のゲームモードと比較
        GameMode lastGameMode = history.peekLast();
        if (lastGameMode != currentGameMode) {
            // 履歴が2つある場合は古い方を削除
            if (history.size() >= 2) {
                history.removeFirst();
            }
            history.addLast(currentGameMode);
            return true;
        }

        return false;
    }

    /**
     * サーバーのティック終了時のイベントハンドラ
     * プレイヤーのゲームモードをリアルタイムで監視し、必要な処理を行います
     *
     * @param event サーバーティック終了イベント
     */
    @EventHandler
    private void onServerTickEnd(ServerTickEndEvent event) {
        // サーバー上の全プレイヤーを取得
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!OSGPlayerUtils.isPlayerEnabled(player)) {
                continue;
            }

            GameMode currentGameMode = player.getGameMode();
            boolean gameModeChanged = updateGameModeHistory(player, currentGameMode);

            // スペクテイターモードのプレイヤーのアイテムフレームは必ず削除
            if (currentGameMode == GameMode.SPECTATOR) {
                ItemFrame itemFrame = ItemFrameUtils.getPlayerItemFrame(player);
                if (itemFrame != null) {
                    ItemFrameUtils.removePlayerItemFrame(player);
                }
                continue;
            }

            // ゲームモードが変更された場合のみ処理を実行
            if (gameModeChanged) {
                ItemFrame itemFrame = ItemFrameUtils.getPlayerItemFrame(player);

                // クリエイティブモードの場合、アイテムフレームを削除
                if (currentGameMode == GameMode.CREATIVE && itemFrame != null) {
                    ItemFrameUtils.removePlayerItemFrame(player);
                }
                // それ以外のモードで、アイテムフレームが存在しない場合は新規生成
                else if (currentGameMode != GameMode.CREATIVE && itemFrame == null) {
                    BuffType buffType = BuffSystem.getRandomBuff(player).getBuffType();
                    ItemFrameUtils.spawnItemFrame(player.getWorld(), player, buffType.getItemStack());
                }
            }
        }
    }

    /**
     * エンティティがダメージを受けたときのイベントハンドラ
     */
    @EventHandler()
    private void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // プレイヤーが有効でない場合は通常のダメージ処理を行う
            if (!OSGPlayerUtils.isPlayerEnabled(player)) {
                return;
            }

            // 矢によるダメージの場合
            if (event.getDamageSource().getCausingEntity() != null &&
                    event.getDamageSource().getCausingEntity().getType() == EntityType.ARROW) {

                // 攻撃者が有効なプレイヤーの場合のみ処理
                if (event.getDamageSource().getCausingEntity() instanceof Player attacker &&
                        OSGPlayerUtils.isPlayerEnabled(attacker)) {
                    event.setCancelled(true);
                }
                return;
            }

            // その他の通常のダメージ（プレイヤーの攻撃、落下など）はそのまま処理
            return;
        }
    }

    /**
     * 矢が何かに当たったときのイベントハンドラ
     * <p>
     * 矢がヒットしたときの処理を行います。
     * - アイテムフレームに当たった場合、アイテムフレームの所有者にダメージを与え、攻撃者にバフを付与します。
     * - それ以外の場合は矢のエンティティを削除します。
     * </p>
     * 
     * @param event 矢のヒットイベント
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.ARROW) {
            return;
        }

        var arrow = event.getEntity();
        if (!(arrow.getShooter() instanceof Player attacker)) {
            return;
        }

        // ヒットしたエンティティがアイテムフレームの場合の処理
        if (event.getHitEntity() instanceof ItemFrame itemFrame) {
            // プラグイン製のアイテムフレームかチェック
            if (!ItemFrameUtils.isPluginItemFrame(itemFrame)) {
                return;
            }

            // メタデータが存在し、空でないことを確認
            if (!itemFrame.hasMetadata("owner") || itemFrame.getMetadata("owner").isEmpty()) {
                return;
            }

            // アイテムフレームの所有者プレイヤーを取得
            Player player = itemFrame.getWorld().getEntitiesByClass(Player.class).stream()
                    .filter(entity -> entity.getUniqueId().toString()
                            .equals(itemFrame.getMetadata("owner").get(0).asString()))
                    .findFirst().orElse(null);

            // プレイヤーが無効な場合は処理しない
            if (player == null || !OSGPlayerUtils.isPlayerEnabled(player)) {
                return;
            }

            // 所有者と攻撃者が同じ場合は何もしない
            if (player.equals(attacker)) {
                return;
            }

            player.damage(4);

            // チームの確認
            boolean isDifferentTeam = true; // デフォルトでは別チームとみなす
            Team playerTeam = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            Team attackerTeam = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(attacker.getName());

            // 両方が同じチームに所属していて、そのチームがフレンドリーファイアを許可していない場合
            if (playerTeam != null && attackerTeam != null && playerTeam.equals(attackerTeam) &&
                    !playerTeam.allowFriendlyFire()) {
                isDifferentTeam = false;
            }

            // 同じチームの場合は処理をキャンセル
            if (!isDifferentTeam) {
                return;
            }

            // アイテムフレームのアイテムを保存しておく（バフ付与用）
            var displayedItem = itemFrame.getItem();

            itemFrame.remove();
            arrow.remove();

            // スコア加算
            if (OSGPlayerUtils.isPlayerEnabled(attacker)) {
                objective.getScore(attacker.getName()).setScore(objective.getScore(attacker.getName()).getScore() + 1);
            }

            // アイテムフレームのアイテムからバフを適用 (別チームのプレイヤーの場合のみ)
            if (isDifferentTeam && !displayedItem.getType().isAir()) {
                BuffType buffType = BuffType.getBuffTypeByItemStack(displayedItem);

                // バフシステムを初期化
                BuffSystem buffSystem = new BuffSystem(buffType);

                if (attackerTeam != null) {
                    // チームのメンバー全員にバフを適用
                    for (String entry : attackerTeam.getEntries()) {
                        Player teammate = Bukkit.getPlayer(entry);
                        if (teammate != null && teammate.isOnline() && OSGPlayerUtils.isPlayerEnabled(teammate)) {
                            buffSystem.applyBuff(teammate);
                            teammate.sendMessage(
                                    Component.text(teammate.getName() + "に" + buffType.getName() + "を付与しました。"));
                        }
                    }
                } else {
                    // チームに所属していない場合は攻撃者のみにバフを適用
                    buffSystem.applyBuff(attacker);
                }

                ItemFrameUtils.removePlayerItemFrame(player);
                ItemFrameUtils.spawnItemFrame(player.getWorld(), player,
                        BuffSystem.getRandomBuff(player).getBuffType().getItemStack());
            }
        } else {
            // アイテムフレーム以外に当たった場合は矢を消去
            if (OSGPlayerUtils.isPlayerEnabled(attacker)) {
                arrow.remove();
            }
        }
    }

    /**
     * プレイヤーが移動したときのイベントハンドラ
     * <p>
     * プレイヤーが移動したとき、そのプレイヤーに関連付けられたItemFrameの位置を更新します。
     * ItemFrameはプレイヤーの頭上2ブロックの位置に配置されます。
     * </p>
     * 
     * @param event プレイヤー移動イベント
     */
    @EventHandler()
    private void onPlayerMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        var location = player.getLocation();

        // ItemFrameUtilsを使用してプレイヤーのアイテムフレームを取得
        ItemFrame itemFrame = ItemFrameUtils.getPlayerItemFrame(player);

        location.setY(location.getY() + 2);

        if (itemFrame != null) {
            itemFrame.teleport(location);
        }
    }

    /**
     * エンティティがテレポートしたときのイベントハンドラ
     * <p>
     * プレイヤーがテレポートしたとき、そのプレイヤーに関連付けられたItemFrameの位置を更新します。
     * ItemFrameはプレイヤーの頭上2ブロックの位置に配置されます。
     * </p>
     * 
     * @param event エンティティテレポートイベント
     */
    @EventHandler()
    private void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!OSGPlayerUtils.isPlayerEnabled(player)) {
            return;
        }

        var location = event.getTo();
        ItemFrame itemFrame = ItemFrameUtils.getPlayerItemFrame(player);

        if (itemFrame != null) {
            location.setY(location.getY() + 2);
            itemFrame.teleport(location);
        }
    }

    /**
     * プレイヤーが死亡したときのイベントハンドラ
     * <p>
     * プレイヤーが死亡したとき、そのプレイヤーに関連付けられたターゲットを削除します。
     * </p>
     * 
     * @param event プレイヤー死亡イベント
     */
    @EventHandler()
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // GameManagerのユーティリティメソッドを使用してターゲットを削除
        GameManager.removeTarget(player);
    }

    /**
     * プレイヤーがリスポーンしたときのイベントハンドラ
     * <p>
     * プレイヤーがリスポーンしたとき、新しいターゲットを生成し、
     * 一時的な耐性効果をプレイヤーに付与します。耐性の持続時間は
     * 設定ファイルのrespawn_set_health_delayの値に基づいて決定されます。
     * </p>
     * 
     * @param event プレイヤーリスポーンイベント
     */
    @EventHandler()
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        var plugin = One_Shot_Glory.getPlugin();

        if (plugin == null) {
            return;
        }

        var player = event.getPlayer();
        if (OSGPlayerUtils.isPlayerEnabled(player)) {
            GameManager.spawnTarget(player);

            int delay = plugin.getConfig().getInt("respawn_set_health_delay");
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (delay * 3) + 100, 255));
        }
    }

    /**
     * プレイヤーがサーバーから退出したときのイベントハンドラ
     * <p>
     * プレイヤーがサーバーから退出したとき、そのプレイヤーに関連付けられたItemFrameを削除します。
     * これにより、サーバー上に残骸となるアイテムフレームが残らないようにします。
     * </p>
     * 
     * @param event プレイヤー退出イベント
     */
    @EventHandler()
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // ItemFrameUtilsを使用してプレイヤーのアイテムフレームを削除
        ItemFrameUtils.removePlayerItemFrame(player);

        // プレイヤーのゲームモード履歴を削除
        playerGameModeHistory.remove(player.getUniqueId());
    }

    /**
     * エンティティからポーション効果が取り除かれたときのイベントハンドラ
     * <p>
     * プレイヤーからバフシステムのポーション効果が取り除かれた場合、
     * 対応するバフも削除します。
     * </p>
     * 
     * @param event ポーション効果除去イベント
     */
    @EventHandler
    private void onPotionEffectRemove(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player && event.getOldEffect() != null) {
            PotionEffectType removedEffect = event.getOldEffect().getType();

            // 各バフタイプをチェック
            for (BuffType buffType : BuffType.values()) {
                if (buffType.getPotionEffectTypes().contains(removedEffect)) {
                    // 直接バフを削除
                    List<BuffType> activeBuffs = BuffSystem.getActiveBuffs(player);
                    if (activeBuffs.remove(buffType)) {
                        // バフリストを更新
                        if (activeBuffs.isEmpty()) {
                            player.removeMetadata(BuffSystem.BUFF_METADATA_KEY, One_Shot_Glory.getPlugin());
                        } else {
                            player.setMetadata(BuffSystem.BUFF_METADATA_KEY,
                                    new FixedMetadataValue(One_Shot_Glory.getPlugin(), activeBuffs));
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * プレイヤーの装備変更イベントハンドラ
     * プレイヤーが弓を持っているかどうかに応じてアイテムフレームの当たり判定を制御
     */
    @EventHandler
    private void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        ItemFrame itemFrame = ItemFrameUtils.getPlayerItemFrame(player);
        if (itemFrame == null)
            return;

        // 弓を持っている場合は当たり判定を無効に
        if (newItem != null && newItem.getType() == Material.BOW) {
            setItemFrameCollision(itemFrame, false);
        } else {
            setItemFrameCollision(itemFrame, true);
        }
    }
}
