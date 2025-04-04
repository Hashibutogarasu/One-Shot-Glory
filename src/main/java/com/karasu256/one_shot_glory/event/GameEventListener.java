package com.karasu256.one_shot_glory.event;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.util.ArmorStandUtils;
import com.karasu256.one_shot_glory.util.BuffSystem;
import com.karasu256.one_shot_glory.util.BuffType;
import com.karasu256.one_shot_glory.util.GameManager;
import com.karasu256.one_shot_glory.util.OSGPlayerUtils;

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
        PlayerGameModeChangeEvent.getHandlerList().unregister(this); // 追加

        // タスクを停止
        if (collisionCheckTaskId != -1) {
            One_Shot_Glory.getPlugin().getServer().getScheduler().cancelTask(collisionCheckTaskId);
            collisionCheckTaskId = -1;
        }

        // 状態管理マップをクリア
        armorStandCollisionStates.clear();
    }

    /**
     * 矢が何かに当たったときのイベントハンドラ
     * <p>
     * 矢がヒットしたときに、矢のエンティティを削除します。
     * </p>
     * 
     * @param event 矢のヒットイベント
     */
    @EventHandler()
    private void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.ARROW) {
            var entity = event.getEntity();
            
            // 矢の発射者が有効なプレイヤーの場合のみ矢を消去
            if (entity.getShooter() instanceof Player shooter && 
                OSGPlayerUtils.getEnabledList().contains(shooter)) {
                entity.remove();
            }
        }
    }

    /**
     * エンティティがダメージを受けたときのイベントハンドラ
     * <p>
     * ArmorStandがダメージを受けた場合、その所有者プレイヤーにダメージを与え、
     * 攻撃したプレイヤーにスコアを加算します。また、ArmorStandの頭部アイテムに基づいて
     * 攻撃者にバフ効果を付与します。
     * </p>
     * 
     * @param event エンティティダメージイベント
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
                    player.damage(1000, DamageSource.builder(DamageType.ARROW).build());
                    event.setCancelled(true);
                }
                return;
            }
            
            // その他の通常のダメージ（プレイヤーの攻撃、落下など）はそのまま処理
            return;
        }

        // アーマースタンドへのダメージ処理
        if (event.getEntity() instanceof ArmorStand armorStand) {
            // プラグイン製のアーマースタンドかチェック
            if (!ArmorStandUtils.isPluginArmorStand(armorStand)) {
                return;
            }

            // 矢によるダメージ以外はキャンセル
            if (!(event.getDamageSource().getCausingEntity() instanceof Arrow arrow)) {
                event.setCancelled(true);
                return;
            }

            // 矢の発射者を取得
            if (!(arrow.getShooter() instanceof Player attacker)) {
                return;
            }

            // メタデータが存在し、空でないことを確認
            if (!armorStand.hasMetadata("owner") || armorStand.getMetadata("owner").isEmpty()) {
                return;
            }
            
            // アーマースタンドの所有者プレイヤーを取得
            var player = armorStand.getWorld().getEntitiesByClass(Player.class).stream()
                    .filter(entity -> entity.getUniqueId().toString()
                            .equals(armorStand.getMetadata("owner").get(0).asString()))
                    .findFirst().orElse(null);

            // プレイヤーが無効な場合は処理しない
            if (player == null || !OSGPlayerUtils.isPlayerEnabled(player)) {
                return;
            }

            // 所有者と攻撃者が同じ場合は何もしない
            if (player.equals(attacker)) {
                return;
            }
            
            // チームの確認
            boolean isDifferentTeam = true;  // デフォルトでは別チームとみなす
            Team playerTeam = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            Team attackerTeam = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(attacker.getName());
            
            // 両方が同じチームに所属していて、そのチームがフレンドリーファイアを許可していない場合
            if (playerTeam != null && attackerTeam != null && playerTeam.equals(attackerTeam) && 
                !playerTeam.allowFriendlyFire()) {
                isDifferentTeam = false;
            }
            
            // 同じチームの場合はダメージをキャンセル
            if (!isDifferentTeam) {
                event.setCancelled(true);
                return;
            }
            
            // ダメージ処理
            player.damage(1000, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
            armorStand.remove();

            // スコア加算
            if (OSGPlayerUtils.isPlayerEnabled(attacker)) {
                objective.getScore(attacker.getName()).setScore(objective.getScore(attacker.getName()).getScore() + 1);
            }

            // アーマースタンドの頭装備からバフを適用 (別チームのプレイヤーの場合のみ)
            if (isDifferentTeam) {
                var itemStack = armorStand.getEquipment().getHelmet();
                BuffType buffType = BuffType.getBuffTypeByItemStack(itemStack);

                // 攻撃者にバフを適用
                BuffSystem buffSystem = new BuffSystem(buffType);
                buffSystem.applyBuff(attacker);
            }
        }
    }

    /**
     * プレイヤーが移動したときのイベントハンドラ
     * <p>
     * プレイヤーが移動したとき、そのプレイヤーに関連付けられたArmorStandの位置を更新します。
     * ArmorStandはプレイヤーの頭上2ブロックの位置に配置されます。
     * </p>
     * 
     * @param event プレイヤー移動イベント
     */
    @EventHandler()
    private void onPlayerMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        var location = player.getLocation();

        // ArmorStandUtilsを使用してプレイヤーのアーマースタンドを取得
        ArmorStand armorStand = ArmorStandUtils.getPlayerArmorStand(player);

        location.setY(location.getY() + 2);

        if (armorStand != null) {
            armorStand.teleport(location);
        }
    }

    /**
     * プレイヤーが死亡したときのイベントハンドラ
     * <p>
     * プレイヤーが死亡したとき、そのプレイヤーに関連付けられたArmorStandを削除します。
     * </p>
     * 
     * @param event プレイヤー死亡イベント
     */
    @EventHandler()
    private void onPlayerDeath(PlayerDeathEvent event) {
        // プレイヤーが死亡したときに、そのプレイヤーのアーマースタンドを削除する
        Player player = event.getEntity();
        
        // ArmorStandUtilsを使用してプレイヤーのアーマースタンドを削除
        ArmorStandUtils.removePlayerArmorStand(player);
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

        // spawn new item display
        if (plugin != null) {
            var player = event.getPlayer();
            GameManager.spawnTarget(player.getWorld(), player);
        }

        // give a effect of resistance to the player
        var player = event.getPlayer();
        if (plugin != null) {
            int delay = plugin.getConfig().getInt("respawn_set_health_delay");

            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (delay * 3) + 100, 255));
        }
    }

    /**
     * プレイヤーがサーバーから退出したときのイベントハンドラ
     * <p>
     * プレイヤーがサーバーから退出したとき、そのプレイヤーに関連付けられたArmorStandを削除します。
     * これにより、サーバー上に残骸となるアーマースタンドが残らないようにします。
     * </p>
     * 
     * @param event プレイヤー退出イベント
     */
    @EventHandler()
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // ArmorStandUtilsを使用してプレイヤーのアーマースタンドを削除
        ArmorStandUtils.removePlayerArmorStand(player);
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
                        
                        // 関連するポーション効果を削除
                        ArmorStand armorStand = ArmorStandUtils.getPlayerArmorStand(player);
                        if (armorStand != null) {
                            for (PotionEffectType effectType : buffType.getPotionEffectTypes()) {
                                armorStand.removePotionEffect(effectType);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * プレイヤーのゲームモードが変更されたときのイベントハンドラ
     * <p>
     * プレイヤーがスペクテイターモードに変更されたとき、
     * そのプレイヤーに関連付けられたArmorStandを削除します。
     * </p>
     *
     * @param event プレイヤーのゲームモード変更イベント
     */
    @EventHandler
    private void onGameModeChange(PlayerGameModeChangeEvent event) {
        // スペクテイターモードに変更される場合
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            Player player = event.getPlayer();
            
            // プレイヤーのアーマースタンドを削除
            ArmorStandUtils.removePlayerArmorStand(player);
        }
    }
}
